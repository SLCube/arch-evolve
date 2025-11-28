package com.playground.auth.contract.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * 인증된 사용자의 상세 정보를 담는 DTO (UserDetails Contract).
 *
 * 이 클래스는 Spring Security 컨텍스트에 저장되는 신원 정보의 계약이며,
 *
 * 시스템의 다른 모듈이 인증된 사용자를 식별하는 표준화된 수단으로 사용됩니다.
 *
 * @property userId 시스템에서 사용되는 사용자의 고유 ID (식별의 핵심 값)
 * @property loginId 사용자의 로그인 ID
 * @property password 사용자의 해싱된 비밀번호 (인증 목적으로만 사용됨)
 * @property roles 사용자가 가진 권한 목록 (예: "ADMIN", "USER")
 */
class AuthUserDetails(
    private val userId: Long,
    private val loginId: String,
    private val password: String,
    private val roles: List<String>,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = roles.map { SimpleGrantedAuthority("ROLE_$it") }

    override fun getPassword(): String = password

    override fun getUsername(): String = loginId

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    /**
     * [Security Principal] 인증된 사용자의 고유 ID를 반환합니다.
     * Service Layer에서 리소스 소유권 검증 등 비즈니스 로직에 사용됩니다.
     */
    fun getUserId(): Long = userId
}