package com.marian.owncloudbackend.service;

import com.marian.owncloudbackend.DTO.UserDTO;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.repository.FileEntityRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileStoreService {

    private final FileEntityRepository fileEntityRepository;
    private final UserService userService;

    public Object uploadNewFile(MultipartFile file) throws IOException {
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
        Path finalPath = Paths.get(baseDir, userByEmail.getUsername(),file.getOriginalFilename());
        File fileToSave = finalPath.toFile();

        var fileEntity = new FileEntity(fileName, finalPath.toString(), suffix, size, userByEmail);
        file.transferTo(fileToSave);



        return null;
    }

    public boolean createUserDirectory(UserDTO userDTO) {
        String baseDir = FileStoreUtils.getBaseDir();
        Path userPath = Paths.get(baseDir,userDTO.username());
        File file = userPath.toFile();
        return file.mkdir();
    }
}
