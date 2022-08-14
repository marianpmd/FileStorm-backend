package com.marian.owncloudbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;

public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {

    Page<FileEntity> findByDirectoryAndUser(DirectoryEntity directory, UserEntity user, Pageable pageable);

    Optional<FileEntity> findByIdAndUser(Long id, UserEntity user);

    Optional<FileEntity> findByName(String name);

    List<FileEntity> findByUserAndNameContaining(UserEntity user, String name);
}