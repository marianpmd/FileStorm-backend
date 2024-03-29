package com.marian.owncloudbackend.entity;

import com.marian.owncloudbackend.enums.FileType;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Lazy;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

@Table(name = "file_entity")
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    private String path;
    private BigInteger size;
    private LocalDateTime lastModified;

    private Boolean isPublic = false;

    @Lob
    private Byte[] thumbnail;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "directory_id")
    private DirectoryEntity directory;

    public FileEntity(String name, String path, BigInteger size,LocalDateTime lastModified, FileType fileType, UserEntity user) {
        this.name = name;
        this.path = path;
        this.lastModified = lastModified;
        this.size = size;
        this.fileType = fileType;
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