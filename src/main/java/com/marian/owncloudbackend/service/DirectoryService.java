package com.marian.owncloudbackend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.marian.owncloudbackend.DTO.DirectoryDTO;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.mapper.DirectoryEntityMapper;
import com.marian.owncloudbackend.repository.DirectoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final DirectoryRepository directoryRepository;
    private final UserService userService;
    private final DirectoryEntityMapper directoryEntityMapper;

    public DirectoryDTO createDirectory(List<String> pathsFromRoot) {

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        Path directoryPath = FileStoreUtils.computePathFromRoot(userEmail, pathsFromRoot);

        DirectoryEntity createdDirectory = null;
        try {
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

    public List<DirectoryDTO> getAllDirectoriesInPath(ArrayList<String> pathsFromRoot) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        Path directoryPath = FileStoreUtils.computePathFromRoot(userEmail, pathsFromRoot);

        List<DirectoryEntity> byFiles_path = directoryRepository.findByPathContainsAndUser(directoryPath.toString(), userByEmail);
        List<DirectoryEntity> correctDirs = new ArrayList<>();

        try (Stream<Path> stream = Files.list(directoryPath)) {
            Set<String> collect = stream
                    .filter(Files::isDirectory)
                    .map(Path::toString)
                    .collect(Collectors.toSet());

            for (DirectoryEntity directoryEntity : byFiles_path) {
                for (String correctPath : collect) {
                    if (directoryEntity.getPath().equals(correctPath)) {
                        correctDirs.add(directoryEntity);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return directoryEntityMapper.entityListToDtoList(correctDirs);
    }
}
