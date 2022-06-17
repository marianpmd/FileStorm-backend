package com.marian.owncloudbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.UserEntity;

@Repository
public interface DirectoryRepository extends JpaRepository<DirectoryEntity, Long> {
    List<DirectoryEntity> findByUser(UserEntity user);
    Optional<DirectoryEntity> findByPath(String path);
    List<DirectoryEntity> findByPathContainsAndUser(String path, UserEntity user);

}
