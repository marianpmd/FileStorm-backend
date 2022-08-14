package com.marian.owncloudbackend.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.marian.owncloudbackend.dto.FileEntityDTO;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.enums.FileType;
import com.marian.owncloudbackend.exceptions.DirectoryNotFoundException;
import com.marian.owncloudbackend.exceptions.FileDoesNotExistException;
import com.marian.owncloudbackend.exceptions.FileEntityNotFoundException;
import com.marian.owncloudbackend.exceptions.FileIsNotPublicException;
import com.marian.owncloudbackend.exceptions.OutOfSpaceException;
import com.marian.owncloudbackend.mapper.FileEntityMapper;
import com.marian.owncloudbackend.repository.FileEntityRepository;
import com.marian.owncloudbackend.utils.FileStoreUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStoreService {

    private final FileEntityRepository fileEntityRepository;
    private final FileEntityMapper fileEntityMapper;
    public final UserService userService;
    private final DirectoryService directoryService;
    private final SimpMessagingTemplate template;

    public File getFileByIdAndUser(Long id, String userEmail) {
        UserEntity userByEmail = userService.getUserByEmail(userEmail);
        FileEntity byIdAndUser = fileEntityRepository.findByIdAndUser(id, userByEmail)
                .orElseThrow(() -> new FileEntityNotFoundException("The requested file was not found in DB!"));

        File file = FileUtils.getFile(byIdAndUser.getPath());

        if (!file.exists()) {
            throw new FileDoesNotExistException("The file " + file + " does not exist on FS!");
        }

        return file;
    }

    public File getFileByIdPublic(Long id) {
        FileEntity byIdAndUser = fileEntityRepository.findById(id)
                .orElseThrow(() -> new FileEntityNotFoundException("The requested file was not found in DB!"));

        File file = FileUtils.getFile(byIdAndUser.getPath());

        if (!file.exists()) {
            throw new FileDoesNotExistException("The file " + file + " does not exist on FS!");
        }
        if (Boolean.FALSE.equals(byIdAndUser.getIsPublic())) {
            throw new FileIsNotPublicException("This file is not public!");
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

    public FileEntityDTO uploadNewFile(MultipartFile file, List<String> pathFromRoot, Boolean shouldUpdate) throws IOException {
        BigInteger size = BigInteger.valueOf(file.getSize());

        String fileName = file.getOriginalFilename();

        String contentType = file.getContentType();
        FileType filetype = FileStoreUtils.getFileTypeFromContentType(contentType);

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = this.userService.getUserByEmail(userEmail);

        BigInteger assignedSpace = userByEmail.getAssignedSpace();
        BigInteger occupiedSpace = userByEmail.getOccupiedSpace();

        if (occupiedSpace.add(assignedSpace).compareTo(size) < 0) {
            throw new OutOfSpaceException("Not enough storage assigned to this user!");
        }

        String pathToDir = FileStoreUtils.computePathFromRoot(userByEmail.getEmail(), pathFromRoot).toString();
        boolean isDir = FileUtils.isDirectory(new File(pathToDir));
        if (!isDir) {
            throw new DirectoryNotFoundException("Directory not found!");
        }
        Path finalPath = Paths.get(pathToDir, file.getOriginalFilename());
        File fileToSave = finalPath.toFile();

        FileEntity fileEntity = new FileEntity(fileName, finalPath.toString(), size, LocalDateTime.now(), filetype, userByEmail);

        DirectoryEntity directoryEntity = directoryService.getDirectoryEntityFromNameAndUser(userByEmail, pathFromRoot)
                .orElseThrow(() -> new DirectoryNotFoundException("Directory was not found for user : " + userEmail
                        + "and path : " + pathFromRoot));

        fileEntity.setDirectory(directoryEntity);
        FileEntity saved;
        if (fileToSave.exists() && shouldUpdate.equals(Boolean.TRUE)) {
            log.info("File aready exists so replace on FS and update on DB");

            Files.delete(fileToSave.toPath());
            try(OutputStream outputStream = Files.newOutputStream(fileToSave.toPath())) {
                IOUtils.copyLarge(file.getInputStream(), outputStream);
            }catch (Exception e){
                log.error("Exception when copying file {}, {}",fileEntity,e);
            }
            FileEntity existingFileEntity = fileEntityRepository.findByName(fileName)
                    .orElseThrow(() -> new FileEntityNotFoundException("File was supposed to exist in the DB, SYNC ERROR."));

            FileEntity newFile = FileEntity.builder()
                    .name(existingFileEntity.getName())
                    .path(existingFileEntity.getPath())
                    .size(size)
                    .lastModified(LocalDateTime.now())
                    .fileType(existingFileEntity.getFileType())
                    .user(existingFileEntity.getUser())
                    .directory(existingFileEntity.getDirectory())
                    .build();

            fileEntityRepository.delete(existingFileEntity);
            saved = fileEntityRepository.save(newFile);

        } else {
            try(OutputStream outputStream = Files.newOutputStream(fileToSave.toPath())){

                IOUtils.copyLarge(file.getInputStream(), outputStream);
            }catch (Exception e){
                log.error("Exception when copying file that is new {} , {} ",fileEntity,e);
            }
            saved = fileEntityRepository.save(fileEntity);
        }

        userService.recomputeUserStorage(userByEmail);
        FileEntityDTO fileEntityDTO = fileEntityMapper.fileEntityToFileEntityDTO(saved);
        template.convertAndSendToUser(userEmail, "/queue/newFile", fileEntityDTO);
        return fileEntityDTO;
    }

    public boolean createUserDirectory(UserEntity userEntity) {
        String baseDir = FileStoreUtils.getBaseDir();
        Path userPath = Paths.get(baseDir, userEntity.getEmail());
        directoryService.createInitialDirectoryEntity(userPath, userEntity);
        File file = userPath.toFile();
        return file.mkdir();
    }

    public Page<FileEntityDTO> getAllFilesForUser(String userEmail, String sortBy, int page, int size, boolean asc, List<String> pathFromRoot) {
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        PageRequest pageable;

        if (asc) {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        }

        DirectoryEntity directoryEntity = directoryService.getDirectoryEntityFromNameAndUser(userByEmail, pathFromRoot)
                .orElseThrow();
        Page<FileEntity> byUser = fileEntityRepository.findByDirectoryAndUser(directoryEntity, userByEmail, pageable);

        return byUser.map(FileEntityDTO::fromEntity);
    }

    public boolean deleteFileByIdAndUser(Long id, String userEmail) {
        UserEntity userByEmail = userService.getUserByEmail(userEmail);
        FileEntity fileByIdAndUser = this.getFileEntityByIdAndUser(id, userByEmail);
        File fileById = getFileById(id);
        fileEntityRepository.delete(fileByIdAndUser);

        var deleted = deleteFileFromFS(fileById);
        if (deleted) userService.recomputeUserStorage(userByEmail);
        return deleted;
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

    public boolean checkIfExists(String filename, List<String> pathFromRoot) {
        String userEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal().toString();

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

    public FileEntityDTO makeFilePublic(Long id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);
        FileEntity fileEntity = fileEntityRepository.findByIdAndUser(id, userByEmail)
                .orElseThrow(() -> new FileDoesNotExistException("File with id " + id + "from user : " + userEmail + "not found."));
        fileEntity.setIsPublic(true);
        FileEntity entity = fileEntityRepository.save(fileEntity);
        return fileEntityMapper.fileEntityToFileEntityDTO(entity);
    }

    public FileEntityDTO makeFilePrivate(Long id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);
        FileEntity fileEntity = fileEntityRepository.findByIdAndUser(id, userByEmail)
                .orElseThrow(() -> new FileDoesNotExistException("File with id " + id + "from user : " + userEmail + "not found."));
        fileEntity.setIsPublic(false);
        FileEntity entity = fileEntityRepository.save(fileEntity);
        return fileEntityMapper.fileEntityToFileEntityDTO(entity);
    }


}
