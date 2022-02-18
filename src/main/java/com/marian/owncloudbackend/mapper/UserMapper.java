package com.marian.owncloudbackend.mapper;

import com.marian.owncloudbackend.DTO.UserDTO;
import com.marian.owncloudbackend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO entityToDTO(UserEntity userEntity);
    UserEntity DTOtoEntity(UserDTO userDTO);
    List<UserDTO> entitiesToDTOs(List<UserEntity> userEntities);

}
