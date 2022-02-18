package com.marian.owncloudbackend.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Table(name = "file_entity")
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;
    private String path;
    private String suffix;
    private Long size;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public FileEntity(String name, String path, String suffix, Long size, UserEntity user) {
        this.name = name;
        this.path = path;
        this.suffix = suffix;
        this.size = size;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        FileEntity that = (FileEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}