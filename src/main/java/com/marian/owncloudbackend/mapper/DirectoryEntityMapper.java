package com.marian.owncloudbackend.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.marian.owncloudbackend.dto.DirectoryDTO;
import com.marian.owncloudbackend.entity.DirectoryEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DirectoryEntityMapper {
    DirectoryEntity directoryDTOToDirectoryEntity(DirectoryDTO directoryDTO);

    DirectoryDTO directoryEntityToDirectoryDTO(DirectoryEntity directoryEntity);

    List<DirectoryDTO> entityListToDtoList(List<DirectoryEntity> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDirectoryEntityFromDirectoryDTO(DirectoryDTO directoryDTO, @MappingTarget DirectoryEntity directoryEntity);
}
