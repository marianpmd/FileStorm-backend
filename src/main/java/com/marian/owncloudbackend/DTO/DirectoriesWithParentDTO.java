package com.marian.owncloudbackend.DTO;

import java.util.List;

import lombok.Builder;

public record DirectoriesWithParentDTO(DirectoryDTO parent, List<DirectoryDTO> directories) {
    @Builder
    public DirectoriesWithParentDTO {
    }
}
