package com.marian.owncloudbackend.DTO;

import lombok.Builder;

@Builder
public record SystemInfoDTO(Long totalSpace,Long usableSpace) {
}
