package com.marian.owncloudbackend.DTO;

import java.io.Serializable;

import lombok.Data;


public record DirectoryDTO(Long id, String path, String name) implements Serializable {
}
