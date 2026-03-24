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
