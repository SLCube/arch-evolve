package com.playground.user.persistence.repository

import com.playground.user.persistence.entity.QUserAddressJpaEntity.Companion.userAddressJpaEntity as address
import com.playground.user.persistence.entity.QUserJpaEntity.Companion.userJpaEntity as user
import com.playground.user.persistence.entity.UserJpaEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.Optional

class UserQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): UserQueryRepository {
    override fun findWithAddressById(userId: Long): Optional<UserJpaEntity> {
        val userJpaEntity = queryFactory.selectFrom(user)
            .join(user.addressEntities, address).fetchJoin()
            .where(user.id.eq(userId))
            .fetchOne()

        return Optional.ofNullable(userJpaEntity)
    }
}