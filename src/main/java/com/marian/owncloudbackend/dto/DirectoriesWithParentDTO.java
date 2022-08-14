package com.marian.owncloudbackend.dto;

import java.util.List;

import lombok.Builder;

public record DirectoriesWithParentDTO(DirectoryDTO parent, List<DirectoryDTO> directories) {
    @Builder
    public DirectoriesWithParentDTO {
        // This is empty because I need to use the @Builder annotation on the constructor
    }
}
