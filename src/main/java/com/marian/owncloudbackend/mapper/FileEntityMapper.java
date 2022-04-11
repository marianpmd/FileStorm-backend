package com.marian.owncloudbackend.mapper;

import com.marian.owncloudbackend.DTO.FileEntityDTO;
import com.marian.owncloudbackend.entity.FileEntity;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FileEntityMapper {
    FileEntity fileEntityDTOToFileEntity(FileEntityDTO fileEntityDTO);

    FileEntityDTO fileEntityToFileEntityDTO(FileEntity fileEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFileEntityFromFileEntityDTO(FileEntityDTO fileEntityDTO, @MappingTarget FileEntity fileEntity);

    List<FileEntityDTO> entitiesToDTOs(List<FileEntity> entities);

}
