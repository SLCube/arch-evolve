package com.playground.user.contract.application.port.outbound

import com.playground.user.contract.domain.vo.UserInfo

/**
 * 사용자 도메인의 핵심 정보를 조회하는 아웃바운드 포트.
 *
 * 주로 인증(로그인) 및 다른 도메인(Order, Delivery)에서 사용자의 프로필 정보를
 * 안전하게 가져가기 위한 계약입니다.
 */
fun interface UserInfoQueryPort {
    /**
     * [로그인 ID]를 기준으로 사용자 정보를 조회합니다.
     * * 데이터가 존재함을 보장하며, 사용자를 찾을 수 없을 경우 반드시 [UserNotFoundException]을 던져야 합니다.
     *
     * @param loginId 사용자의 로그인 ID (유일값)
     * @return UserInfo (Non-nullable)
     */
    fun getUserInfoByLoginId(loginId: String): UserInfo
}