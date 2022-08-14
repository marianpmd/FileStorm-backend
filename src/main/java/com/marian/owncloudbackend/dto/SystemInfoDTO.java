package com.marian.owncloudbackend.dto;

import lombok.Builder;

@Builder
public record SystemInfoDTO(Long totalSpace,Long usableSpace) {
}
