package com.marian.owncloudbackend.mapper;

import com.marian.owncloudbackend.dto.UserDTO;
import com.marian.owncloudbackend.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO entityToDTO(UserEntity userEntity);
    List<UserDTO> entitiesToDTOs(List<UserEntity> userEntities);

}
