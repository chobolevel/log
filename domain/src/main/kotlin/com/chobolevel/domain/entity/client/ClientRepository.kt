package com.chobolevel.domain.entity.client

import org.springframework.data.jpa.repository.JpaRepository

interface ClientRepository : JpaRepository<Client, Long> {

    fun findByIdAndUserIdAndDeletedFalse(id: String, userId: Long): Client?
}
