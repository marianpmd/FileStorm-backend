package com.marian.owncloudbackend.service.directory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.marian.owncloudbackend.dto.DirectoriesWithParentDTO;
import com.marian.owncloudbackend.dto.DirectoryDTO;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.mapper.DirectoryEntityMapper;
import com.marian.owncloudbackend.mapper.UserMapper;
import com.marian.owncloudbackend.repository.DirectoryRepository;
import com.marian.owncloudbackend.repository.UserRepository;
import com.marian.owncloudbackend.service.BaseUnit;
import com.marian.owncloudbackend.service.UserService;
import com.marian.owncloudbackend.utils.FileStoreUtils;
import com.marian.owncloudbackend.utils.SecurityContextUtils;


class DirectoryServiceTest extends BaseUnit {
    @Mock
    private DirectoryRepository directoryRepository;
    @Mock
    private UserService userService;
    @Spy
    private DirectoryEntityMapper directoryEntityMapper = Mappers.getMapper(DirectoryEntityMapper.class);
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContextUtils securityContextUtils;
    @InjectMocks
    private com.marian.owncloudbackend.service.DirectoryService sut;

    public static Stream<Arguments> providePathList() {
        return Stream.of(
                Arguments.of(List.of("alex", "personal")),
                Arguments.of(List.of("spaced ", "last", " place ")),
                Arguments.of(List.of("unsanitized\\", "last")),
                Arguments.of(List.of("alex ", "/", "\\personal"))
        );
    }

    @AfterEach
    void clearDirs() throws IOException {
        FileUtils.deleteDirectory(
                new File(FileStoreUtils.computePathFromRoot(TEST_MAIL, Collections.emptyList()).toString()));

    }

    @ParameterizedTest
    @MethodSource("providePathList")
    void createDirectory(List<String> paths) {
        //Arrange
        Path testDir = FileStoreUtils.computePathFromRoot(TEST_MAIL, paths);
        String pathString = testDir.toString();
        DirectoryDTO expected = new DirectoryDTO(1L, pathString, paths.get(paths.size() - 1));
        when(securityContextUtils.getUserEmail()).thenReturn(TEST_MAIL);
        when(userService.getUserByEmail(anyString())).thenReturn(new UserEntity(TEST_MAIL, "aaa", "ADMIN"));
        // return the same entity
        when(directoryRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        //Act
        DirectoryDTO result = sut.createDirectory(paths);
        //Assert
        assertThat(result.path()).isEqualTo(expected.path());
        assertThat(result.name()).isEqualTo(expected.name());
        assertThat(testDir).exists();
    }

    @ParameterizedTest
    @MethodSource("providePathList")
    void getAllDirectoriesInPathWithParent(List<String> paths) throws IOException {
        //Arrange
        String testInnerDirName = "testInnerDirName";
        when(securityContextUtils.getUserEmail()).thenReturn(TEST_MAIL);
        Path testDir = FileStoreUtils.computePathFromRoot(TEST_MAIL, paths);
        Path innerDir = Paths.get(testDir.toAbsolutePath().toString(), testInnerDirName);
        when(directoryRepository.findByPathContainsAndUser(anyString(), any())).thenReturn(
                List.of(
                        new DirectoryEntity(innerDir.toAbsolutePath().toString(), testInnerDirName, null)
                )
        );
        // The parent of the last dir is the one before last in the path
        when(directoryRepository.findByPath(anyString())).thenReturn(Optional.of(
                new DirectoryEntity(testDir.getParent().toAbsolutePath().toString(), paths.get(paths.size() - 2), null)
        ));
        FileUtils.forceMkdir(testDir.toFile());
        FileUtils.forceMkdir(innerDir.toFile());


        //Act
        DirectoriesWithParentDTO result = sut.getAllDirectoriesInPathWithParent(paths);


        //Assert
        assertThat(result.parent().path()).isEqualTo(testDir.getParent().toAbsolutePath().toString());
        assertThat(result.directories())
                .contains(new DirectoryDTO(null, innerDir.toAbsolutePath().toString(), testInnerDirName));
        assertThat(testDir).exists();
        assertThat(innerDir).exists();

    }

    @Test
    void deleteDirById() throws IOException {
        //Arrange
        Path testPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("test", "userStuff"));
        Path testPathFile1 = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("test", "userStuff", "TEST1"));
        Path testPathFile2 = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("test", "userStuff", "TEST2"));
        FileUtils.touch(testPathFile1.toFile());
        FileUtils.touch(testPathFile2.toFile());

        DirectoryEntity directoryEntity = new DirectoryEntity(testPath.toAbsolutePath().toString(), "userStuff", null);

        when(directoryRepository.findById(anyLong())).thenReturn(Optional.of(directoryEntity));

        //Act
        DirectoryDTO result = sut.deleteDirById(1L);

        //Assert
        assertThat(testPath).doesNotExist();
        assertThat(testPathFile1).doesNotExist();
        assertThat(testPathFile2).doesNotExist();
        assertThat(result.path()).isEqualTo(testPath.toAbsolutePath().toString());

    }

}