package com.playground.payment.consumer.support

import com.playground.payment.common.log.utils.logger
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.springframework.kafka.listener.BatchInterceptor
import org.springframework.stereotype.Component

@Component
class KafkaConsumerBatchLoggingInterceptor : BatchInterceptor<Any, Any> {
    private val log = logger()

    override fun intercept(
        records: ConsumerRecords<Any, Any>,
        consumer: Consumer<Any, Any>,
    ): ConsumerRecords<Any, Any> {
        log.info("배치 수신 [size={}]", records.count())
        return records
    }
}
