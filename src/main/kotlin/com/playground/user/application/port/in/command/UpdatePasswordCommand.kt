package com.playground.user.application.port.`in`.command

data class UpdatePasswordCommand(
    val userId: Long,
    val oldPassword: String,
    val newPassword: String
) {
    override fun toString(): String {
        return "UpdatePasswordCommand(userId=$userId, oldPassword='****', newPassword='****')"
    }
}