package com.playground.product.infra.redis.client

import org.springframework.data.redis.core.script.DefaultRedisScript

/**
 * Redis 재고 관리를 위한 Lua Script 정의
 */
internal object StockLuaScripts {
    const val STOCK_KEY_PREFIX = "product:stock:"
    const val AVAILABLE_KEY_SUFFIX = ":available"
    const val RESERVED_KEY_SUFFIX = ":reserved"
    const val CONFIRMED_KEY_SUFFIX = ":confirmed"
    const val DIRTY_SET_KEY = "product:stock:dirty"

    /**
     * KEYS[1]은 주문별 예약 Hash이며 이후 상품별 available, reserved, confirmed 키가 이어진다.
     * ARGV[1]은 정렬·합산한 상품 수량 기록이며 이후 상품별 수량이 이어진다.
     * 성공·동일 예약 재시도 시 0, 예약 내용 충돌 시 -1, 재고 부족 시 상품의 1-based 위치를 반환한다.
     */
    private const val RESERVE_STOCKS_SCRIPT_TEXT =
        """
local existingItems = redis.call('HGET', KEYS[1], 'items')
if existingItems then
    if existingItems ~= ARGV[1] then
        return -1
    end
    if redis.call('HGET', KEYS[1], 'status') == 'RELEASED' then
        return -2
    end
    return 0
end

for i = 1, #ARGV - 1 do
    local keyIndex = (i - 1) * 3 + 1
    local available = tonumber(redis.call('GET', KEYS[keyIndex + 1]) or '0')
    local reserved = tonumber(redis.call('GET', KEYS[keyIndex + 2]) or '0')
    local confirmed = tonumber(redis.call('GET', KEYS[keyIndex + 3]) or '0')
    local quantity = tonumber(ARGV[i + 1])

    if available - reserved - confirmed < quantity then
        return i
    end
end

for i = 1, #ARGV - 1 do
    local keyIndex = (i - 1) * 3 + 1
    redis.call('INCRBY', KEYS[keyIndex + 2], ARGV[i + 1])
end

redis.call('HSET', KEYS[1], 'items', ARGV[1], 'status', 'RESERVED')

return 0
        """

    val RESERVE_STOCKS_SCRIPT: DefaultRedisScript<Long> =
        DefaultRedisScript<Long>().apply {
            setScriptText(RESERVE_STOCKS_SCRIPT_TEXT)
            resultType = Long::class.java
        }

    /** KEYS: 예약 Hash, dirty Set, 상품별 reserved/confirmed. ARGV: items, 목표 상태, 상품별 수량/ID. */
    val TRANSITION_RESERVATION_SCRIPT: DefaultRedisScript<Long> =
        DefaultRedisScript<Long>().apply {
            setScriptText(
                """
local items = redis.call('HGET', KEYS[1], 'items')
local target = ARGV[2]
if not items then
    if target == 'RELEASED' then
        redis.call('HSET', KEYS[1], 'items', ARGV[1], 'status', target)
        return 0
    end
    return -2
end
if items ~= ARGV[1] then return -1 end
local state = redis.call('HGET', KEYS[1], 'status')
if state == target then return 0 end
if state ~= 'RESERVED' then return -2 end
local count = (#ARGV - 2) / 2
for i = 1, count do
    local reserved = tonumber(redis.call('GET', KEYS[i * 2 + 1]) or '0')
    local confirmed = tonumber(redis.call('GET', KEYS[i * 2 + 2]) or '0')
    local quantity = tonumber(ARGV[i * 2 + 1])
    if not confirmed then return -2 end
    if reserved < quantity then return i end
end
for i = 1, count do
    local quantity = ARGV[i * 2 + 1]
    redis.call('DECRBY', KEYS[i * 2 + 1], quantity)
    if target == 'CONFIRMED' then
        redis.call('INCRBY', KEYS[i * 2 + 2], quantity)
        redis.call('SADD', KEYS[2], ARGV[i * 2 + 2])
    end
end
redis.call('HSET', KEYS[1], 'status', target)
return 0
                """,
            )
            resultType = Long::class.java
        }

    /**
     * Lua Script: 재고 예약 (3단계 재고 관리 - Step 1)
     *
     * KEYS[1]: product:stock:{productId}:available
     * KEYS[2]: product:stock:{productId}:reserved
     * KEYS[3]: product:stock:{productId}:confirmed
     * ARGV[1]: quantity
     *
     * 반환: 성공 시 남은 판매 가능 재고, 실패 시 -1
     */
    private const val RESERVE_STOCK_SCRIPT_TEXT =
        """
local available = tonumber(redis.call('GET', KEYS[1]) or '0')
local reserved = tonumber(redis.call('GET', KEYS[2]) or '0')
local confirmed = tonumber(redis.call('GET', KEYS[3]) or '0')
local quantity = tonumber(ARGV[1])

local sellableStock = available - reserved - confirmed

if sellableStock >= quantity then
    redis.call('INCRBY', KEYS[2], quantity)
    return sellableStock - quantity
else
    return -1
end
        """

    /**
     * Lua Script: 재고 확정 (3단계 재고 관리 - Step 2)
     *
     * KEYS[1]: product:stock:{productId}:reserved
     * KEYS[2]: product:stock:{productId}:confirmed
     * KEYS[3]: product:stock:dirty
     * ARGV[1]: quantity
     * ARGV[2]: productId
     *
     * 반환: 성공 시 confirmed 값, 실패 시 -1 (reserved 부족)
     */
    private const val CONFIRM_STOCK_SCRIPT_TEXT =
        """
local quantity = tonumber(ARGV[1])
local productId = ARGV[2]

local reserved = tonumber(redis.call('GET', KEYS[1]) or '0')

if reserved < quantity then
    return -1
end

redis.call('DECRBY', KEYS[1], quantity)
redis.call('INCRBY', KEYS[2], quantity)
redis.call('SADD', KEYS[3], productId)

return tonumber(redis.call('GET', KEYS[2]))
        """

    /**
     * Lua Script: 예약 해제 (3단계 재고 관리 - Step 3)
     *
     * KEYS[1]: product:stock:{productId}:reserved
     * ARGV[1]: quantity
     *
     * 반환: 성공 시 reserved 값, 실패 시 -1 (reserved 부족)
     */
    private const val RELEASE_RESERVED_STOCK_SCRIPT_TEXT =
        """
local quantity = tonumber(ARGV[1])

local reserved = tonumber(redis.call('GET', KEYS[1]) or '0')

if reserved < quantity then
    return -1
end

redis.call('DECRBY', KEYS[1], quantity)

return tonumber(redis.call('GET', KEYS[1]))
        """

    /**
     * Lua Script: dirty set의 모든 productId를 원자적으로 읽고 삭제
     *
     * KEYS[1]: product:stock:dirty
     *
     * 반환: 삭제된 productId 목록 (String List)
     */
    private const val GET_DIRTY_AND_CLEAR_SCRIPT_TEXT =
        """
local members = redis.call('SMEMBERS', KEYS[1])
if #members > 0 then
    redis.call('DEL', KEYS[1])
end
return members
        """

    val RESERVE_STOCK_SCRIPT: DefaultRedisScript<Long> =
        DefaultRedisScript<Long>().apply {
            setScriptText(RESERVE_STOCK_SCRIPT_TEXT)
            resultType = Long::class.java
        }

    val CONFIRM_STOCK_SCRIPT: DefaultRedisScript<Long> =
        DefaultRedisScript<Long>().apply {
            setScriptText(CONFIRM_STOCK_SCRIPT_TEXT)
            resultType = Long::class.java
        }

    val RELEASE_RESERVED_STOCK_SCRIPT: DefaultRedisScript<Long> =
        DefaultRedisScript<Long>().apply {
            setScriptText(RELEASE_RESERVED_STOCK_SCRIPT_TEXT)
            resultType = Long::class.java
        }

    val GET_DIRTY_AND_CLEAR_SCRIPT: DefaultRedisScript<List<*>> =
        DefaultRedisScript<List<*>>().apply {
            setScriptText(GET_DIRTY_AND_CLEAR_SCRIPT_TEXT)
            resultType = List::class.java
        }
}
