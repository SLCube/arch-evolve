package com.playground.product.contract.application.port.outbound.customer

import com.playground.product.contract.domain.vo.ProductInfo

/**
 * 상품 도메인의 외부 정보 제공용 아웃바운드 포트.
 *
 * 외부 도메인에서 상품의 핵심 정보(가격, 재고, 이름 등)를
 * 참조해야 할 때 사용하는 계약 인터페이스입니다.
 *
 * 내부 구현체(Entity)가 아닌 불변 객체인 [com.playground.product.contract.domain.vo.ProductInfo] VO를 반환하여
 * 도메인 간의 결합도를 낮춥니다.
 */
fun interface ProductInfoQueryPort {
    /**
     * 다수의 상품 ID를 받아 해당 상품들의 상세 정보를 일괄 조회합니다.
     *
     * @param productIds 조회할 상품 ID 목록 (List)
     * @return 상품 ID를 Key로, [com.playground.product.contract.domain.vo.ProductInfo]를 Value로 하는 Map.
     *
     * *주의: 요청한 ID 중 존재하지 않는 상품은 Map의 결과에 포함되지 않습니다.*
     */
    fun getProductInfos(productIds: List<Long>): Map<Long, ProductInfo>
}