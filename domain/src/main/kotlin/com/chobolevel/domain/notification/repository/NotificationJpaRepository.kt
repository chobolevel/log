package com.chobolevel.domain.notification.repository

import com.chobolevel.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationJpaRepository : JpaRepository<Notification, Long>
