package com.marian.owncloudbackend.DTO;

import com.marian.owncloudbackend.enums.FileType;
import lombok.Data;

import java.io.Serializable;

@Data
public class FileEntityDTO implements Serializable {
    private final Long id;
    private final String name;
    private final String path;
    private final String suffix;
    private final Long size;
    private final FileType fileType;
}
