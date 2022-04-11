package com.marian.owncloudbackend.repository;

import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
    Page<FileEntity> findByUser(UserEntity user, Pageable pageable);

    Optional<FileEntity> findByIdAndUser(Long id, UserEntity user);

    Optional<FileEntity> findByName(String name);




}