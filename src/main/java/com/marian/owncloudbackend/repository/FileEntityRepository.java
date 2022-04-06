package com.marian.owncloudbackend.repository;

import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByUser(UserEntity user);

    Optional<FileEntity> findByIdAndUser(Long id, UserEntity user);


}