package com.marian.owncloudbackend.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import com.marian.owncloudbackend.enums.NotificationState;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "notification_entity")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String description;

    @CreationTimestamp
    private LocalDateTime dateTime;

    @ManyToOne
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    private NotificationState notificationState = NotificationState.UNREAD;
    public NotificationEntity(String description, UserEntity userEntity) {
        this.description = description;
        this.userEntity = userEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        NotificationEntity that = (NotificationEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}