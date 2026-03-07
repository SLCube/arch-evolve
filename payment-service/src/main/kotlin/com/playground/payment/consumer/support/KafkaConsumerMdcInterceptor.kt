package com.playground.payment.consumer.support

import com.playground.payment.common.log.mdc.HeaderKeys
import com.playground.payment.common.log.mdc.MdcKeys
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.listener.RecordInterceptor
import org.springframework.stereotype.Component

@Component
class KafkaConsumerMdcInterceptor : RecordInterceptor<Any, Any> {
    override fun intercept(
        record: ConsumerRecord<Any, Any>,
        consumer: Consumer<Any, Any>,
    ): ConsumerRecord<Any, Any> {
        val requestId =
            record
                .headers()
                .lastHeader(HeaderKeys.X_REQUEST_ID)
                ?.value()
                ?.let { String(it) }
                ?: ""
        MDC.put(MdcKeys.REQUEST_ID, requestId)
        return record
    }

    override fun afterRecord(
        record: ConsumerRecord<Any, Any>,
        consumer: Consumer<Any, Any>,
    ) {
        MDC.remove(MdcKeys.REQUEST_ID)
    }
}
