package com.marian.owncloudbackend.dto;

import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.enums.FileType;

import java.io.Serializable;
import java.math.BigInteger;


public record FileEntityDTO(Long id, String name, String path, BigInteger size,
                            FileType fileType,Boolean isPublic) implements Serializable {


    public static FileEntityDTO fromEntity(FileEntity entity) {
        return new FileEntityDTO(entity.getId(),
                entity.getName(),
                entity.getPath(),
                entity.getSize(),
                entity.getFileType(),
                entity.getIsPublic());
    }
}
