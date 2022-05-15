package com.marian.owncloudbackend.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.marian.owncloudbackend.DTO.NotificationDTO;
import com.marian.owncloudbackend.entity.NotificationEntity;

@Mapper(componentModel = "spring")
public interface NotificationEntityMapper {
    NotificationDTO entityToDTO(NotificationEntity entity);

    NotificationEntity DTOtoEntity(NotificationDTO dto);

    List<NotificationDTO> entitiesToDTO(List<NotificationEntity> entities);
}
