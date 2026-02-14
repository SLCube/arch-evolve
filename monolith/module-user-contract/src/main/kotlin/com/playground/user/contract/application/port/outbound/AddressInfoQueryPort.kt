package com.playground.user.contract.application.port.outbound

import com.playground.user.contract.domain.vo.ReceiverAddressInfo

/**
 * 사용자 도메인의 특정 주소 정보를 조회하는 아웃바운드 포트 계약.
 *
 * 주로 [Order]와 같은 외부 도메인에서 주문 시점의 배송지 주소 생성을 위해 사용됩니다.
 */
fun interface AddressInfoQueryPort {
    /**
     * [userId]를 기준으로 해당 사용자가 소유한 특정 주소([addressId])의 상세 정보를 조회합니다.
     *
     * 1. **소유권 검증 필수:** 해당 [addressId]가 실제로 [userId]의 소유인지 반드시 검증해야 합니다.
     * 2. **불변 데이터 반환:** 반환되는 데이터는 주소 스냅샷(VO)으로, 이 값을 수정한 후에도 [UserService]의 원본 주소에는 영향이 없어야 합니다.
     *
     * @param userId 주소의 소유자 ID (소유권 검증에 사용)
     * @param addressId 조회할 주소의 고유 ID
     * @return AddressSnapshotVO (도로명, 상세주소 등을 포함하는 불변 값 객체)
     * @throws AddressNotFoundException 주소를 찾을 수 없거나 소유권이 일치하지 않을 경우
     */
    fun getAddressInfoByAddressId(userId: Long, addressId: Long): ReceiverAddressInfo
}