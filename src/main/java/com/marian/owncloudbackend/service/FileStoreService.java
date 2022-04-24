package com.marian.owncloudbackend.service;

import com.marian.owncloudbackend.DTO.FileEntityDTO;
import com.marian.owncloudbackend.DTO.UserDTO;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.enums.FileType;
import com.marian.owncloudbackend.exceptions.FileDoesNotExistException;
import com.marian.owncloudbackend.exceptions.FileEntityNotFoundException;
import com.marian.owncloudbackend.mapper.FileEntityMapper;
import com.marian.owncloudbackend.repository.FileEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStoreService {

    private final FileEntityRepository fileEntityRepository;
    private final FileEntityMapper fileEntityMapper;
    private final UserService userService;

    public File getFileByIdAndUser(Long id, UserEntity user) {
        FileEntity byIdAndUser = fileEntityRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new FileEntityNotFoundException("The requested file was not found in DB!"));

        File file = FileUtils.getFile(byIdAndUser.getPath());

        if (!file.exists()) {
            throw new FileDoesNotExistException("The file " + file + " does not exist on FS!");
        }

        return file;
    }

    public File getFileById(Long id) {
        FileEntity byId = fileEntityRepository.findById(id)
                .orElseThrow(() -> new FileEntityNotFoundException("The requested file was not found in DB!"));

        File file = FileUtils.getFile(byId.getPath());

        if (!file.exists()) {
            throw new FileDoesNotExistException("The file " + file + " does not exist on FS!");
        }

        return file;
    }

    public FileEntity getFileEntityByIdAndUser(Long id, UserEntity user) {
        FileEntity byIdAndUser = fileEntityRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new FileEntityNotFoundException("The requested file was not found in DB!"));

        File file = FileUtils.getFile(byIdAndUser.getPath());

        if (!file.exists()) {
            throw new FileDoesNotExistException("The file " + file + " does not exist on FS!");
        }

        return byIdAndUser;
    }

    public FileEntityDTO uploadNewFile(MultipartFile file, Boolean shouldUpdate) throws IOException {
        BigInteger size = BigInteger.valueOf(file.getSize());
        String fileName = file.getOriginalFilename();

        String contentType = file.getContentType();
        FileType filetype = FileStoreUtils.getFileTypeFromContentType(contentType);

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = this.userService.getUserByEmail(userEmail);
        if (userByEmail == null) {
            return null; //todo handle accordingly
        }

        String baseDir = FileStoreUtils.getBaseDir();
        Path finalPath = Paths.get(baseDir, userByEmail.getEmail(), file.getOriginalFilename());
        File fileToSave = finalPath.toFile();

        FileEntity fileEntity = new FileEntity(fileName, finalPath.toString(), size, LocalDateTime.now(), filetype, userByEmail);//todo handle file type by suffix

        FileEntity saved = null;
        if (fileToSave.exists() && shouldUpdate.equals(Boolean.TRUE)) {
            log.info("File aready exists so replace on FS and update on DB");
            if (fileToSave.delete()) {
                IOUtils.copyLarge(file.getInputStream(), Files.newOutputStream(fileToSave.toPath()));
                FileEntity existingFileEntity = fileEntityRepository.findByName(fileName)
                        .orElseThrow(() -> new FileEntityNotFoundException("File was supposed to exist in the DB, SYNC ERROR."));
                existingFileEntity.setLastModified(LocalDateTime.now());
                saved = fileEntityRepository.save(existingFileEntity);
            }
        } else {
            IOUtils.copyLarge(file.getInputStream(), Files.newOutputStream(fileToSave.toPath()));
            saved = fileEntityRepository.save(fileEntity);
        }
        return fileEntityMapper.fileEntityToFileEntityDTO(saved);
    }

    public boolean createUserDirectory(UserDTO userDTO) {
        String baseDir = FileStoreUtils.getBaseDir();
        Path userPath = Paths.get(baseDir, userDTO.email());
        File file = userPath.toFile();
        return file.mkdir();
    }

    public Page<FileEntityDTO> getAllFilesForUser(String userEmail, String sortBy, int page, int size, boolean asc) {
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        PageRequest pageable;

        if (asc) {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        }
        Page<FileEntity> byUser = fileEntityRepository.findByUser(userByEmail, pageable);

        return byUser.map(FileEntityDTO::fromEntity);
    }

    public boolean deleteFileByIdAndUser(Long id, UserEntity user) {
        FileEntity fileByIdAndUser = this.getFileEntityByIdAndUser(id, user);
        File fileById = getFileById(id);
        fileEntityRepository.delete(fileByIdAndUser);

        return deleteFileFromFS(fileById);
    }

    private boolean deleteFileFromFS(File file) {
        try {
            FileUtils.forceDelete(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean checkIfExists(String filename) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = this.userService.getUserByEmail(userEmail);
        String baseDir = FileStoreUtils.getBaseDir();
        Path finalPath = Paths.get(baseDir, userByEmail.getEmail(), filename);
        File fileToSave = finalPath.toFile();

        if (fileToSave.exists()) {
            log.warn("File {} already exists for user {}", fileToSave, userEmail);
            return true;
        }
        return false;
    }

    public List<FileEntityDTO> getAllFilesLike(String keyword) {
        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);
        List<FileEntity> byUserAndNameContaining = fileEntityRepository.findByUserAndNameContaining(userByEmail, keyword);

        return fileEntityMapper.entitiesToDTOs(byUserAndNameContaining);
    }
}
