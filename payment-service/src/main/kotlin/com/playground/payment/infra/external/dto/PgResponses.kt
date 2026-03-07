package com.playground.payment.infra.external.dto

data class BillingKeyResponse(
    val billingKey: String,
)

data class PgAuthorizeResponse(
    val pgTransactionId: String,
    val approvalNumber: String,
    val status: String,
)
