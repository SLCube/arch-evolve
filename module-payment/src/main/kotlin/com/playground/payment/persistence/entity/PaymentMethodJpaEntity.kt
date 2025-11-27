package com.playground.payment.persistence.entity

import com.playground.common.persistence.jpa.BaseEntity
import com.playground.payment.domain.model.PaymentMethod
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "payment_methods")
class PaymentMethodJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id")
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val billingKey: String,

    @Column(nullable = false)
    val cardCompany: String,

    @Column(nullable = false)
    val cardNumberMasked: String,

    @Column(nullable = false)
    var isDefault: Boolean,
) : BaseEntity() {
    companion object {
        fun toJpaEntity(domain: PaymentMethod) : PaymentMethodJpaEntity {
            return PaymentMethodJpaEntity(
                id = domain.id,
                userId = domain.userId,
                billingKey = domain.billingKey,
                cardCompany = domain.cardCompany,
                cardNumberMasked = domain.cardNumberMasked,
                isDefault = domain.isDefault
            )
        }
    }

    fun updateFromDomain(domain: PaymentMethod) {
        this.isDefault = domain.isDefault
    }
}