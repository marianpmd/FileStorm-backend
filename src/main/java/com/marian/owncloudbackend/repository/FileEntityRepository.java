package com.marian.owncloudbackend.repository;

import com.marian.owncloudbackend.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
}