package com.marian.owncloudbackend.dto;

import java.io.Serializable;


public record DirectoryDTO(Long id, String path, String name) implements Serializable {
}
