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
    const val RESERVATION_KEY_PREFIX = "${STOCK_KEY_PREFIX}reservation:"
    const val ITEMS_FIELD = "items"
    const val STATUS_FIELD = "status"
    const val RESERVED_STATE = "RESERVED"
    const val CONFIRMED_STATE = "CONFIRMED"
    const val RELEASED_STATE = "RELEASED"
    const val SUCCESS = 0L
    const val ITEMS_CONFLICT = -1L
    const val INVALID_STATE = -2L
    const val PENDING_RESERVATIONS_KEY = "product:stock:reservations:pending"

    val DEFER_RECOVERY_SCRIPT = DefaultRedisScript<Long>(
        """
if redis.call('HGET', KEYS[1], '$STATUS_FIELD') == '$RESERVED_STATE' then
    redis.call('ZADD', KEYS[2], ARGV[2], ARGV[1])
else
    redis.call('ZREM', KEYS[2], ARGV[1])
end
return 0
        """,
        Long::class.java,
    )

    val REINDEX_RESERVATION_SCRIPT = DefaultRedisScript<Long>(
        """
if redis.call('HGET', KEYS[1], '$STATUS_FIELD') == '$RESERVED_STATE' then
    redis.call('ZADD', KEYS[2], 'NX', 0, ARGV[1])
end
return 0
        """,
        Long::class.java,
    )

    val INITIALIZE_STOCK_SCRIPT = DefaultRedisScript<Long>(
        """
for i = 1, #ARGV do
    local base = (i - 1) * 3
    redis.call('SETNX', KEYS[base + 1], ARGV[i])
    redis.call('SETNX', KEYS[base + 2], '0')
    redis.call('SETNX', KEYS[base + 3], '0')
end
return 0
        """,
        Long::class.java,
    )

    val STOCK_SNAPSHOT_SCRIPT = DefaultRedisScript<List<*>>(
        """
local available = tonumber(redis.call('GET', KEYS[1]))
local confirmed = tonumber(redis.call('GET', KEYS[2]) or '0')
if not available then return redis.error_reply('Missing stock baseline') end
return {available - confirmed, confirmed}
        """,
        List::class.java,
    )

    val ACK_STOCK_SYNC_SCRIPT = DefaultRedisScript<Long>(
        """
if (redis.call('GET', KEYS[2]) or '0') == ARGV[2] then
    return redis.call('SREM', KEYS[1], ARGV[1])
end
return 0
        """,
        Long::class.java,
    )

    /**
     * KEYS[1]은 주문별 예약 Hash이며 상품별 available/reserved/confirmed 키와 마지막 복구 ZSet이 이어진다.
     * ARGV[1]은 정렬·합산한 상품 수량 기록, ARGV[2]는 트랜잭션 시도 ID이며 이후 수량이 이어진다.
     * 성공·동일 예약 재시도 시 0, 내용 충돌 -1, 상태 충돌 -2, 재고 부족 시 상품의 1-based 위치를 반환한다.
     */
    private const val RESERVE_STOCKS_SCRIPT_TEXT =
        """
local existingItems = redis.call('HGET', KEYS[1], '$ITEMS_FIELD')
if existingItems then
    if existingItems ~= ARGV[1] then
        return -1
    end
    if redis.call('HGET', KEYS[1], '$STATUS_FIELD') == '$RELEASED_STATE' then
        return -2
    end
    return 0
end

for i = 1, #ARGV - 2 do
    local keyIndex = (i - 1) * 3 + 1
    local available = tonumber(redis.call('GET', KEYS[keyIndex + 1]) or '0')
    local reserved = tonumber(redis.call('GET', KEYS[keyIndex + 2]) or '0')
    local confirmed = tonumber(redis.call('GET', KEYS[keyIndex + 3]) or '0')
    local quantity = tonumber(ARGV[i + 2])

    if available - reserved - confirmed < quantity then
        return i
    end
end

for i = 1, #ARGV - 2 do
    local keyIndex = (i - 1) * 3 + 1
    redis.call('INCRBY', KEYS[keyIndex + 2], ARGV[i + 2])
end

redis.call('HSET', KEYS[1], '$ITEMS_FIELD', ARGV[1], '$STATUS_FIELD', '$RESERVED_STATE')
if ARGV[2] ~= '' then redis.call('HSET', KEYS[1], 'attemptId', ARGV[2]) end
redis.call('ZADD', KEYS[#KEYS], 0, string.match(KEYS[1], '(%d+)$'))

return 0
        """

    val RESERVE_STOCKS_SCRIPT: DefaultRedisScript<Long> =
        DefaultRedisScript<Long>().apply {
            setScriptText(RESERVE_STOCKS_SCRIPT_TEXT)
            resultType = Long::class.java
        }

    /** KEYS: 예약 Hash, dirty Set, 복구 ZSet, 상품별 reserved/confirmed. ARGV: items, 상태, 시도 ID, 수량/ID. */
    val TRANSITION_RESERVATION_SCRIPT: DefaultRedisScript<Long> =
        DefaultRedisScript<Long>().apply {
            setScriptText(
                """
local items = redis.call('HGET', KEYS[1], '$ITEMS_FIELD')
local target = ARGV[2]
if not items then
    if target == '$RELEASED_STATE' then
        redis.call('HSET', KEYS[1], '$ITEMS_FIELD', ARGV[1], '$STATUS_FIELD', target)
        return 0
    end
    return -2
end
if ARGV[3] ~= '' and redis.call('HGET', KEYS[1], 'attemptId') ~= ARGV[3] then return 0 end
if items ~= ARGV[1] then return -1 end
local state = redis.call('HGET', KEYS[1], '$STATUS_FIELD')
if state == target then return 0 end
if state ~= '$RESERVED_STATE' then return -2 end
local count = (#ARGV - 3) / 2
for i = 1, count do
    local reserved = tonumber(redis.call('GET', KEYS[i * 2 + 2]) or '0')
    local confirmed = tonumber(redis.call('GET', KEYS[i * 2 + 3]) or '0')
    local quantity = tonumber(ARGV[i * 2 + 2])
    if not confirmed then return -2 end
    if reserved < quantity then return i end
end
for i = 1, count do
    local quantity = ARGV[i * 2 + 2]
    redis.call('DECRBY', KEYS[i * 2 + 2], quantity)
    if target == '$CONFIRMED_STATE' then
        redis.call('INCRBY', KEYS[i * 2 + 3], quantity)
        redis.call('SADD', KEYS[2], ARGV[i * 2 + 3])
    end
end
redis.call('HSET', KEYS[1], '$STATUS_FIELD', target)
redis.call('ZREM', KEYS[3], string.match(KEYS[1], '(%d+)$'))
return 0
                """,
            )
            resultType = Long::class.java
        }
}
