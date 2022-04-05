package com.marian.owncloudbackend.service;

import com.marian.owncloudbackend.DTO.FileEntityDTO;
import com.marian.owncloudbackend.DTO.UserDTO;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.enums.FileType;
import com.marian.owncloudbackend.mapper.FileEntityMapper;
import com.marian.owncloudbackend.repository.FileEntityRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStoreService {

    private final FileEntityRepository fileEntityRepository;
    private final FileEntityMapper fileEntityMapper;
    private final UserService userService;

    public FileEntityDTO uploadNewFile(MultipartFile file) throws IOException {
        long size = file.getBytes().length;
        String[] nameSuffix = file.getOriginalFilename().split("\\.");
        String fileName = nameSuffix[0];
        String suffix = "";
        if (!StringUtils.isEmpty(nameSuffix[1])){
            suffix = nameSuffix[1];
        }

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = this.userService.getUserByEmail(userEmail);
        if (userByEmail==null){
            return null;
        }

        String baseDir = FileStoreUtils.getBaseDir();
        Path finalPath = Paths.get(baseDir, userByEmail.getEmail(),file.getOriginalFilename());
        File fileToSave = finalPath.toFile();

        FileEntity fileEntity = new FileEntity(fileName, finalPath.toString(), suffix, size, FileType.FILE, userByEmail);

        if (fileToSave.exists()){
            File existingFile = FileUtils.getFile(finalPath.toFile());
            FileUtils.copyFile(fileToSave,existingFile, StandardCopyOption.REPLACE_EXISTING);
        }
        file.transferTo(fileToSave);

        FileEntity saved = fileEntityRepository.save(fileEntity);
        return fileEntityMapper.fileEntityToFileEntityDTO(saved);
    }

    public boolean createUserDirectory(UserDTO userDTO) {
        String baseDir = FileStoreUtils.getBaseDir();
        Path userPath = Paths.get(baseDir,userDTO.email());
        File file = userPath.toFile();
        return file.mkdir();
    }

    public List<FileEntityDTO> getAllFilesForUser(String userEmail) {
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        List<FileEntity> byUser = fileEntityRepository.findByUser(userByEmail);

        return fileEntityMapper.entitiesToDTOs(byUser);
    }
}
