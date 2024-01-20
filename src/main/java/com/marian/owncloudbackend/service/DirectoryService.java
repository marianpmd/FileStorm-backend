package com.marian.owncloudbackend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.marian.owncloudbackend.dto.DirectoriesWithParentDTO;
import com.marian.owncloudbackend.dto.DirectoryDTO;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.exceptions.DirectoryNotFoundException;
import com.marian.owncloudbackend.mapper.DirectoryEntityMapper;
import com.marian.owncloudbackend.repository.DirectoryRepository;
import com.marian.owncloudbackend.utils.FileStoreUtils;
import com.marian.owncloudbackend.utils.SecurityContextUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final DirectoryRepository directoryRepository;
    private final UserService userService;
    private final DirectoryEntityMapper directoryEntityMapper;
    private final SecurityContextUtils securityContextUtils;

    public DirectoryDTO createDirectory(List<String> pathsFromRoot) {

        String userEmail = securityContextUtils.getUserEmail();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        Path directoryPath = FileStoreUtils.computePathFromRoot(userEmail, pathsFromRoot);

        DirectoryEntity createdDirectory = null;
        try {
            if (directoryPath.toFile().exists()) {
                throw new IllegalStateException("Dir already exists at path :" + directoryPath);
            }
            FileUtils.forceMkdir(directoryPath.toFile());
            if (!pathsFromRoot.get(pathsFromRoot.size() - 1).equals(userEmail)) {
                DirectoryEntity directoryEntity = new DirectoryEntity(directoryPath.toString(), pathsFromRoot.get(pathsFromRoot.size() - 1), userByEmail);
                createdDirectory = directoryRepository.save(directoryEntity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return directoryEntityMapper.directoryEntityToDirectoryDTO(createdDirectory);
    }

    public DirectoriesWithParentDTO getAllDirectoriesInPathWithParent(List<String> pathsFromRoot) {
        String userEmail = securityContextUtils.getUserEmail();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        Path directoryPath = FileStoreUtils.computePathFromRoot(userEmail, pathsFromRoot);
        List<DirectoryEntity> listOfPaths = directoryRepository.findByPathContainsAndUser(directoryPath.toString(), userByEmail);
        List<DirectoryEntity> correctDirs = new ArrayList<>();

        try (Stream<Path> stream = Files.list(directoryPath)) {
            Set<String> collect = stream
                    .filter(Files::isDirectory)
                    .map(Path::toString)
                    .collect(Collectors.toSet());

            for (DirectoryEntity directoryEntity : listOfPaths) {
                for (String correctPath : collect) {
                    if (directoryEntity.getPath().equals(correctPath)) {
                        correctDirs.add(directoryEntity);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        DirectoryEntity parentEntity = null;
        if (!CollectionUtils.isEmpty(pathsFromRoot)) {
            Path parent = directoryPath.getParent();
            parentEntity = directoryRepository.findByPath(parent.toString())
                    .orElseThrow(() -> new DirectoryNotFoundException("Directory has no parent."));
        }

        List<DirectoryDTO> directoryDTOS = directoryEntityMapper.entityListToDtoList(correctDirs);
        return DirectoriesWithParentDTO.builder()
                .directories(directoryDTOS)
                .parent(directoryEntityMapper.directoryEntityToDirectoryDTO(parentEntity))
                .build();
    }

    public Optional<DirectoryEntity> getDirectoryEntityFromNameAndUser(UserEntity userByEmail, List<String> pathFromRoot) {
        Path directoryPath = FileStoreUtils.computePathFromRoot(userByEmail.getEmail(), pathFromRoot);
        return directoryRepository.findByPath(directoryPath.toString());
    }

    public void createInitialDirectoryEntity(Path userPath, UserEntity userEntity) {
        DirectoryEntity root = new DirectoryEntity(userPath.toString(), "root", userEntity);
        directoryRepository.save(root);
    }

    public DirectoryDTO deleteDirById(Long id) throws IOException {
        DirectoryEntity directoryEntity = directoryRepository.findById(id)
                .orElseThrow(() -> new DirectoryNotFoundException("Dir not found for deletion"));

        UserEntity user = directoryEntity.getUser();
        File directoryToDelete = Path.of(directoryEntity.getPath()).toFile();
        FileUtils.deleteDirectory(directoryToDelete);
        DirectoryDTO directoryDTO = directoryEntityMapper.directoryEntityToDirectoryDTO(directoryEntity);

        directoryRepository.delete(directoryEntity);

        userService.recomputeUserStorage(user);
        return directoryDTO;
    }

}
