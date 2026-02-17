package com.playground.payment.domain.vo

data class PgAuthorizationResult(
    val isSuccess: Boolean,
    val pgTransactionId: String?,
    val approvalNumber: String?,
    val failReason: String?,
) {
    fun requirePgTransactionId(): String = pgTransactionId ?: throw IllegalStateException("거래 ID 누락")

    fun requireApprovalNumber(): String = approvalNumber ?: throw java.lang.IllegalStateException("승인번호 누락")
}
