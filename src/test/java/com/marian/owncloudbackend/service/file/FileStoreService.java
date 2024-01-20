package com.marian.owncloudbackend.service.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;

import com.marian.owncloudbackend.dto.FileEntityDTO;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.enums.FileType;
import com.marian.owncloudbackend.mapper.FileEntityMapper;
import com.marian.owncloudbackend.repository.FileEntityRepository;
import com.marian.owncloudbackend.service.BaseUnit;
import com.marian.owncloudbackend.service.DirectoryService;
import com.marian.owncloudbackend.service.UserService;
import com.marian.owncloudbackend.utils.FileStoreUtils;
import com.marian.owncloudbackend.utils.SecurityContextUtils;

class FileStoreServiceTest extends BaseUnit {

    @Mock
    private FileEntityRepository fileEntityRepository;
    @Spy
    private FileEntityMapper directoryEntityMapper = Mappers.getMapper(FileEntityMapper.class);
    @Mock
    public UserService userService;
    @Mock
    private DirectoryService directoryService;
    @Mock
    private SimpMessagingTemplate template;

    @Mock
    private SecurityContextUtils securityContextUtils;
    @InjectMocks
    private com.marian.owncloudbackend.service.FileStoreService sut;

    @Test
    void uploadNewFile() throws IOException {
        //Arrange

        File testFile = new ClassPathResource("test_pic.png").getFile();
        when(securityContextUtils.getUserEmail()).thenReturn(TEST_MAIL);
        UserEntity givenUser = new UserEntity(TEST_MAIL, "PASS", "user");
        givenUser.setAssignedSpace(new BigInteger("20000"));
        givenUser.setOccupiedSpace(BigInteger.ZERO);
        Path testDirPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("test", "pics"));
        FileUtils.forceMkdir(testDirPath.toFile());
        DirectoryEntity givenDirectoryEntity = new DirectoryEntity(testDirPath.toAbsolutePath().toString(), testFile.getName(), givenUser);
        FileEntity givenFileEntity = new FileEntity(1L, testFile.getName(), testFile.getAbsolutePath().toString(),
                BigInteger.valueOf(testFile.length()), LocalDateTime.now(), false, null, FileType.IMAGE,
                givenUser, givenDirectoryEntity);
        when(userService.getUserByEmail(anyString())).thenReturn(givenUser);
        when(directoryService.getDirectoryEntityFromNameAndUser(any(), anyList())).thenReturn(Optional.of(givenDirectoryEntity));
        when(fileEntityRepository.save(any())).thenReturn(givenFileEntity);
        FileEntityDTO result;

        //Act
        try (FileInputStream fileInputStream = new FileInputStream(testFile)) {
            MockMultipartFile mockFile = new MockMultipartFile(testFile.getName(), testFile.getName(), "image/png", fileInputStream.readAllBytes());
            result = sut.uploadNewFile(mockFile, List.of("test", "pics"), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Assert
        File actual = new File(givenFileEntity.getPath());
        assertThat(actual).exists();
        assertThat(actual.length()).isEqualTo(testFile.length());
        assertThat(actual.getName()).isEqualTo(result.name());
    }

    @Test
    void uploadNewFile_update() throws IOException {
        //Arrange

        File testFile = new ClassPathResource("test_pic.png").getFile();
        File replacementFile = new ClassPathResource("replacement_pic.png").getFile();
        when(securityContextUtils.getUserEmail()).thenReturn(TEST_MAIL);
        UserEntity givenUser = new UserEntity(TEST_MAIL, "PASS", "user");
        givenUser.setAssignedSpace(new BigInteger("20000"));
        givenUser.setOccupiedSpace(BigInteger.ZERO);
        Path testDirPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("test", "pics"));
        FileUtils.forceMkdir(testDirPath.toFile());
        FileUtils.copyFile(testFile, Paths.get(testDirPath.toFile().getAbsolutePath(), testFile.getName()).toFile());
        DirectoryEntity givenDirectoryEntity = new DirectoryEntity(testDirPath.toAbsolutePath().toString(), testFile.getName(), givenUser);
        FileEntity givenFileEntity = new FileEntity(1L, testFile.getName(), testFile.getAbsolutePath(),
                BigInteger.valueOf(replacementFile.length()), LocalDateTime.now(), false, null, FileType.IMAGE,
                givenUser, givenDirectoryEntity);
        when(userService.getUserByEmail(anyString())).thenReturn(givenUser);
        when(directoryService.getDirectoryEntityFromNameAndUser(any(), anyList())).thenReturn(Optional.of(givenDirectoryEntity));
        when(fileEntityRepository.save(any())).thenReturn(givenFileEntity);
        when(fileEntityRepository.findByName(any())).thenReturn(Optional.of(givenFileEntity));
        FileEntityDTO result;

        //Act

        try (FileInputStream replacementInputStream = new FileInputStream(replacementFile)) {
            MockMultipartFile mockFile = new MockMultipartFile(testFile.getName(), testFile.getName(), "image/png", replacementInputStream);
            result = sut.uploadNewFile(mockFile, List.of("test", "pics"), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Assert
        File actual = Paths.get(testDirPath.toAbsolutePath().toString(), testFile.getName()).toFile();
        assertThat(replacementFile).exists();
        assertThat(actual.length()).isEqualTo(replacementFile.length());
        assertThat(actual.getName()).isEqualTo(result.name());
    }


    @AfterEach
    void clearDirs() throws IOException {
        FileUtils.deleteDirectory(
                new File(FileStoreUtils.computePathFromRoot(TEST_MAIL, Collections.emptyList()).toString()));

    }

}