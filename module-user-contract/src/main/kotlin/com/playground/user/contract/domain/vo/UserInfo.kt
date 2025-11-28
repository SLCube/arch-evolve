package com.playground.user.contract.domain.vo

/**
 * 사용자의 핵심 프로필 정보를 담는 Value Object.
 *
 * 이 DTO는 [User] 도메인이 외부에 노출하는 사용자 정보의 계약입니다.
 * 주로 다른 도메인(Order, Delivery 등)이 사용자 식별이나 기본 정보를 참조할 때 사용됩니다.
 *
 * 비밀번호와 같은 민감한 인증 정보는 포함하지 않습니다.
 *
 * @property userId 사용자의 고유 ID
 * @property loginId 사용자의 로그인 ID
 * @property role 사용자가 가진 권한 (단일 권한을 가정)
 */
data class UserInfo(
    val userId: Long,
    val loginId: String,
    val password: String,
    val role: String,
)
