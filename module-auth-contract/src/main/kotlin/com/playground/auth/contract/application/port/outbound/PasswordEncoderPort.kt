package com.playground.auth.contract.application.port.outbound

/**
 * 비밀번호 인코딩 및 검증을 위한 아웃바운드 포트(Outbound Port).
 *
 * 이 포트는 User 도메인이 Spring Security의 구현체에 직접 의존하는 것을 방지하고,
 *
 * 시스템의 보안 인프라를 추상화하여 격리하는 역할을 합니다.
 *
 * User 도메인은 이 Port를 호출하여 비밀번호를 저장하고 검증하며,
 * Auth 도메인 내부의 Adapter가 실제 인코딩 로직을 구현합니다.
 */
interface PasswordEncoderPort {
    /**
     * 평문 비밀번호를 단방향으로 인코딩하여 저장 가능한 문자열로 변환합니다.
     * 이 메서드는 BCrypt와 같은 해싱 알고리즘을 사용하도록 구현되어야 합니다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @return 단방향 해싱된 비밀번호 문자열 (Non-null 보장)
     */
    fun encode(rawPassword: String): String

    /**
     * 입력된 평문 비밀번호와 저장된 해싱된 비밀번호가 일치하는지 확인합니다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encodedPassword 데이터베이스에 저장된 해싱된 비밀번호
     * @return 비밀번호 일치 여부 (true/false)
     */
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}