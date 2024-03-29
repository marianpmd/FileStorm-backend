package com.marian.owncloudbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marian.owncloudbackend.entity.NotificationEntity;
import com.marian.owncloudbackend.entity.UserEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserEntityOrderByNotificationStateDescDateTimeDesc(UserEntity userEntity);
}
