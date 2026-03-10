package com.playground.payment.infra.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate

fun <T> KafkaTemplate<String, String>.sendAll(
    items: List<T>,
    toRecord: (T) -> ProducerRecord<String, String>,
    onError: (T, Throwable) -> Unit,
): List<T> {
    val futures = items.map { it to send(toRecord(it)) }
    flush()
    return futures.mapNotNull { (item, future) ->
        runCatching {
            future.get()
            item
        }.onFailure { onError(item, it) }
            .getOrNull()
    }
}
