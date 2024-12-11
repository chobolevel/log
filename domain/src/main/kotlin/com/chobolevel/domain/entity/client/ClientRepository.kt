package com.chobolevel.domain.entity.client

import org.springframework.data.jpa.repository.JpaRepository

interface ClientRepository : JpaRepository<Client, Long> {

    fun findByIdAndDeletedFalse(id: String): Client?

    fun findByIdAndUserIdAndDeletedFalse(id: String, userId: Long): Client?
}
