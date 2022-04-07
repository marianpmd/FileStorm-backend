package com.marian.owncloudbackend.DTO;

import com.marian.owncloudbackend.enums.FileType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

@Data
public class FileEntityDTO implements Serializable {
    private final Long id;
    private final String name;
    private final String path;
    private final BigInteger size;
    private final FileType fileType;
}
