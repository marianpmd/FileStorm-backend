package com.marian.owncloudbackend.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;

import java.math.BigInteger;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UserEntity implements Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String email;

    @Transient
    private String username;
    private String password;
    private String role;
    private BigInteger assignedSpace = BigInteger.ZERO;
    private BigInteger occupiedSpace = BigInteger.ZERO;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    List<FileEntity> files;

    @OneToMany(mappedBy = "userEntity",cascade = CascadeType.REMOVE)
    @ToString.Exclude
    List<NotificationEntity> notifications;

    public UserEntity(String email, String password, String role) {
        this.password = password;
        this.role = role;
        this.email = email;
        this.username = setUsername();
    }

    public String setUsername() {
        String[] split = this.email.split("@");
        return split[0]
                .replace("\\/","")
                .replace("\\","");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserEntity that = (UserEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getName() {
        return getEmail();
    }
}
