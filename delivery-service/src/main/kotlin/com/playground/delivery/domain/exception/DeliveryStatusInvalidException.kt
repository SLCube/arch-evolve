package com.playground.delivery.domain.exception

import com.playground.delivery.domain.enum.DeliveryStatus

class DeliveryStatusInvalidException(
    deliveryId: Long?,
    currentStatus: DeliveryStatus,
    targetStatus: DeliveryStatus,
) : BusinessException(
        errorCode = ErrorCode.DELIVERY_STATUS_INVALID,
        message = ErrorCode.DELIVERY_STATUS_INVALID.message(deliveryId, currentStatus, targetStatus),
    )
