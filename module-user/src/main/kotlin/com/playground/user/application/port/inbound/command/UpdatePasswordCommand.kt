package com.playground.user.application.port.inbound.command

data class UpdatePasswordCommand(
    val requestUserId: Long,
    val targetUserId: Long,
    val oldPassword: String,
    val newPassword: String,
) {
    override fun toString(): String = "UpdatePasswordCommand(requestUserId=$requestUserId, targetUserId=$targetUserId, oldPassword='****', newPassword='****')"
}
