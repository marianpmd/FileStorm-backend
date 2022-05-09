package com.marian.owncloudbackend.service;

import com.marian.owncloudbackend.DTO.FileEntityDTO;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.enums.FileType;
import com.marian.owncloudbackend.exceptions.DirectoryNotFoundException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStoreService {

    private final FileEntityRepository fileEntityRepository;
    private final FileEntityMapper fileEntityMapper;
    private final UserService userService;
    private final DirectoryService directoryService;

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

    public FileEntityDTO uploadNewFile(MultipartFile file, ArrayList<String> pathFromRoot, Boolean shouldUpdate) throws IOException {
        BigInteger size = BigInteger.valueOf(file.getSize());
        String fileName = file.getOriginalFilename();

        String contentType = file.getContentType();
        FileType filetype = FileStoreUtils.getFileTypeFromContentType(contentType);

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = this.userService.getUserByEmail(userEmail);
        if (userByEmail == null) {
            return null; //todo handle accordingly
        }

        String pathToDir = FileStoreUtils.computePathFromRoot(userByEmail.getEmail(), pathFromRoot).toString();
        boolean isDir = FileUtils.isDirectory(new File(pathToDir));
        if (!isDir){
            throw new DirectoryNotFoundException("Directory not found!");
        }
        Path finalPath = Paths.get(pathToDir, file.getOriginalFilename());
        File fileToSave = finalPath.toFile();

        FileEntity fileEntity = new FileEntity(fileName, finalPath.toString(), size, LocalDateTime.now(), filetype, userByEmail);

        DirectoryEntity directoryEntity = directoryService.getDirectoryEntityFromNameAndUser(userByEmail, pathFromRoot)
                .orElseThrow(()->new DirectoryNotFoundException("Directory was not found for user : " + userEmail
                + "and path : " + pathFromRoot));

        fileEntity.setDirectory(directoryEntity);
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

    public boolean createUserDirectory(UserEntity userEntity) {
        String baseDir = FileStoreUtils.getBaseDir();
        Path userPath = Paths.get(baseDir, userEntity.getEmail());
        directoryService.createInitialDirectoryEntity(userPath,userEntity);
        File file = userPath.toFile();
        return file.mkdir();
    }

    public Page<FileEntityDTO> getAllFilesForUser(String userEmail, String sortBy, int page, int size, boolean asc, ArrayList<String> pathFromRoot) {
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        PageRequest pageable;

        if (asc) {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        }

        DirectoryEntity directoryEntity = directoryService.getDirectoryEntityFromNameAndUser(userByEmail, pathFromRoot)
                .orElseThrow(); //todo handle
                                //todo there is no root dir saved in the db
        Page<FileEntity> byUser = fileEntityRepository.findByDirectoryAndUser(directoryEntity,userByEmail, pageable);

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

    public boolean checkIfExists(String filename,ArrayList<String> pathFromRoot) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

        pathFromRoot.add(filename);
        Path path = FileStoreUtils.computePathFromRoot(userEmail, pathFromRoot);

        File fileToSave = path.toFile();

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
