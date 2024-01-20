package com.marian.owncloudbackend.service.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.enums.FileType;
import com.marian.owncloudbackend.repository.DirectoryRepository;
import com.marian.owncloudbackend.repository.FileEntityRepository;
import com.marian.owncloudbackend.repository.UserRepository;
import com.marian.owncloudbackend.service.BaseIntegration;
import com.marian.owncloudbackend.service.FileStoreService;
import com.marian.owncloudbackend.service.VideoService;
import com.marian.owncloudbackend.utils.FileStoreUtils;

class FileControllerTest extends BaseIntegration {

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private VideoService videoService;

    @Autowired
    private FileEntityRepository fileEntityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("The case when a new file is added")
    void uploadFile() throws Exception {
        //Arrange
        UserEntity userEntity = createUserWithDefaultDirectory(userRepository, fileStoreService);
        userEntity.setAssignedSpace(new BigInteger("1000"));
        userRepository.save(userEntity);
        File testFile = new ClassPathResource("test_pic.png").getFile();
        try (FileInputStream fileOutputStream = new FileInputStream(testFile)) {
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test_pic.png", "image/png", fileOutputStream.readAllBytes());
            //Act
            MvcResult response = mvc.perform(
                            multipart("/file/upload" + "?pathFromRoot=", Collections.emptyList())
                                    .file(mockMultipartFile)
                                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());

            //Assert
            assertThat(mockMultipartFile.getOriginalFilename()).isEqualTo(jsonNode.get("name").asText());
            assertThat(mockMultipartFile.getSize()).isEqualTo(jsonNode.get("size").asInt());
            assertThat(jsonNode.get("fileType").asText()).isEqualTo("IMAGE");
            assertThat(jsonNode.get("isPublic").asBoolean()).isFalse();
        }
    }

    @Test
    @DisplayName("The case when a file exists and it is updated")
    void uploadFileUpdate() throws Exception {
        //Arrange
        UserEntity userEntity = createUserWithDefaultDirectory(userRepository, fileStoreService);
        userEntity.setAssignedSpace(new BigInteger("1000"));
        userEntity.setOccupiedSpace(new BigInteger("103"));
        userRepository.save(userEntity);
        File testFile = new ClassPathResource("test_pic.png").getFile();
        File replacementFile = new ClassPathResource("replacement_pic.png").getFile();
        FileEntity existingFileEntity = new FileEntity(1L, testFile.getName(), testFile.getAbsolutePath(), new BigInteger("103"), LocalDateTime.of(2024, Month.JANUARY.getValue(), 19, 12, 59), false, null,
                FileType.IMAGE, userEntity, null);
        fileEntityRepository.save(existingFileEntity);
        Path testDirPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, Collections.emptyList());
        FileUtils.forceMkdir(testDirPath.toFile());
        FileUtils.copyFile(testFile, Paths.get(testDirPath.toFile().getAbsolutePath(), testFile.getName()).toFile());

        try (FileInputStream fileOutputStream = new FileInputStream(replacementFile)) {
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test_pic.png", "image/png", fileOutputStream.readAllBytes());
            //Act
            MvcResult response = mvc.perform(
                            multipart("/file/upload" + "?pathFromRoot=&shouldUpdate=true", Collections.emptyList())
                                    .file(mockMultipartFile)
                                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();
            UserEntity modifiedUser = userRepository.findByEmail(TEST_MAIL).orElseThrow();
            JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());

            //Assert
            assertThat(mockMultipartFile.getOriginalFilename()).isEqualTo(jsonNode.get("name").asText());
            assertThat(mockMultipartFile.getSize()).isEqualTo(jsonNode.get("size").asInt());
            assertThat(jsonNode.get("fileType").asText()).isEqualTo("IMAGE");
            assertThat(jsonNode.get("isPublic").asBoolean()).isFalse();
            assertThat(modifiedUser.getOccupiedSpace()).isEqualTo(new BigInteger("156"));
            assertThat(testDirPath.toFile()).exists();
        }
    }

    @Test
    void checkFile() throws Exception {
        //Arrange
        UserEntity userEntity = createUserWithDefaultDirectory(userRepository, fileStoreService);
        userEntity.setAssignedSpace(new BigInteger("1000"));
        userRepository.save(userEntity);
        File testFile = new ClassPathResource("test_pic.png").getFile();
        FileEntity existingFileEntity = new FileEntity(1L, testFile.getName(), testFile.getAbsolutePath(), new BigInteger("103"), LocalDateTime.of(2024, Month.JANUARY.getValue(), 19, 12, 59), false, null,
                FileType.IMAGE, userEntity, null);
        fileEntityRepository.save(existingFileEntity);
        Path testDirPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, Collections.emptyList());
        FileUtils.forceMkdir(testDirPath.toFile());
        FileUtils.copyFile(testFile, Paths.get(testDirPath.toFile().getAbsolutePath(), testFile.getName()).toFile());


        //Act
        MvcResult response = mvc.perform(
                        get("/file/check" + "?pathFromRoot=&filename=test_pic.png", Collections.emptyList())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());

        //Assert
        assertThat(jsonNode.asBoolean()).isTrue();
        assertThat(Path.of(testDirPath.toAbsolutePath().toString(), testFile.getName()).toFile()).exists();

    }

    @Test
    void getAllFilesForUser() throws Exception {
        //Arrange
        UserEntity userEntity = createUserWithDefaultDirectory(userRepository, fileStoreService);
        userEntity.setAssignedSpace(new BigInteger("1000"));
        userEntity.setOccupiedSpace(new BigInteger("103").add(new BigInteger("156")));
        Path testDirPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("inner"));
        UserEntity savedUser = userRepository.save(userEntity);
        DirectoryEntity directoryEntity = new DirectoryEntity(testDirPath.toAbsolutePath().toString(), TEST_MAIL, savedUser);
        DirectoryEntity savedDirectory = directoryRepository.save(directoryEntity);
        File firstFile = new ClassPathResource("test_pic.png").getFile();
        File secondFile = new ClassPathResource("replacement_pic.png").getFile();
        FileEntity firstFileEntity = new FileEntity(1L, firstFile.getName(), firstFile.getAbsolutePath(), new BigInteger("103"), LocalDateTime.of(2024, Month.JANUARY.getValue(), 19, 12, 59), false, null,
                FileType.IMAGE, savedUser, savedDirectory);
        FileEntity secondFileEntity = new FileEntity(2L, secondFile.getName(), secondFile.getAbsolutePath(), new BigInteger("103"), LocalDateTime.of(2024, Month.JANUARY.getValue(), 20, 12, 0), false, null,
                FileType.IMAGE, savedUser, savedDirectory);
        fileEntityRepository.save(firstFileEntity);
        fileEntityRepository.save(secondFileEntity);

        FileUtils.forceMkdir(testDirPath.toFile());
        FileUtils.copyFile(firstFile, Paths.get(testDirPath.toFile().getAbsolutePath(), firstFile.getName()).toFile());
        FileUtils.copyFile(secondFile, Paths.get(testDirPath.toFile().getAbsolutePath(), secondFile.getName()).toFile());


        //Act
        MvcResult response = mvc.perform(
                        get("/file/all" + "?pathFromRoot=inner&sortBy=lastModified&page=0&size=100&asc=true")
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());
        ArrayNode content = ((ArrayNode) jsonNode.get("content"));
        JsonNode firstFileResponse = content.get(0);
        JsonNode secondFileResponse = content.get(1);

        //Assert

        assertThat(firstFileResponse.get("name").asText()).isEqualTo(firstFile.getName());
        assertThat(firstFileResponse.get("size").asInt()).isEqualTo(firstFileEntity.getSize().intValue());
        assertThat(secondFileResponse.get("name").asText()).isEqualTo(secondFile.getName());
        assertThat(secondFileResponse.get("size").asInt()).isEqualTo(secondFileEntity.getSize().intValue());
    }

    @Test
    void getFileFromUserAndId() throws Exception {
        //Arrange
        UserEntity userEntity = createUserWithDefaultDirectory(userRepository, fileStoreService);
        userEntity.setAssignedSpace(new BigInteger("1000"));
        userEntity.setOccupiedSpace(new BigInteger("103"));
        Path testDirPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("inner"));
        UserEntity savedUser = userRepository.save(userEntity);
        DirectoryEntity directoryEntity = new DirectoryEntity(testDirPath.toAbsolutePath().toString(), TEST_MAIL, savedUser);
        DirectoryEntity savedDirectory = directoryRepository.save(directoryEntity);
        File file = new ClassPathResource("test_pic.png").getFile();
        FileEntity fileEntity = new FileEntity(1L, file.getName(), file.getAbsolutePath(), new BigInteger("103"), LocalDateTime.of(2024, Month.JANUARY.getValue(), 19, 12, 59), false, null,
                FileType.IMAGE, savedUser, savedDirectory);
        FileEntity savedFile = fileEntityRepository.save(fileEntity);

        //Act
        MvcResult response = mvc.perform(
                        get("/file/one")
                                .param("id", savedFile.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String header = response.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        byte[] contentAsByteArray = response.getResponse().getContentAsByteArray();


        assertThat(header).contains(savedFile.getName());
        assertThat(contentAsByteArray).hasSize(savedFile.getSize().intValue());

    }

    @Test
    void deleteFileFromUserAndId() throws Exception {
        //Arrange
        UserEntity userEntity = createUserWithDefaultDirectory(userRepository, fileStoreService);
        userEntity.setAssignedSpace(new BigInteger("1000"));
        userEntity.setOccupiedSpace(new BigInteger("103"));
        Path testDirPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("inner"));
        UserEntity savedUser = userRepository.save(userEntity);
        DirectoryEntity directoryEntity = new DirectoryEntity(testDirPath.toAbsolutePath().toString(), TEST_MAIL, savedUser);
        DirectoryEntity savedDirectory = directoryRepository.save(directoryEntity);
//        File file = new ClassPathResource("test_pic.png").getFile();
        Path pathOfFile = Paths.get(testDirPath.toAbsolutePath().toString(), "test_pic.png");
        FileEntity fileEntity = new FileEntity(1L, "test_pic.png", pathOfFile.toString(), new BigInteger("103"), LocalDateTime.of(2024, Month.JANUARY.getValue(), 19, 12, 59), false, null,
                FileType.IMAGE, savedUser, savedDirectory);
        FileEntity savedFile = fileEntityRepository.save(fileEntity);
        FileUtils.forceMkdir(testDirPath.toFile());
        FileUtils.copyFile(new File("src/test/resources/test_pic.png"), Paths.get(testDirPath.toFile().getAbsolutePath(), "test_pic.png").toFile());

        //Act
        mvc.perform(
                        delete("/file/delete/one")
                                .param("id", savedFile.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Optional<FileEntity> afterDeleteFile = fileEntityRepository.findByName(fileEntity.getName());
        UserEntity afterDeleteUser = userRepository.findByEmail(TEST_MAIL).get();

        //Assert
        assertThat(afterDeleteFile).isEmpty();
        assertThat(afterDeleteUser.getOccupiedSpace().intValue()).isZero();
        assertThat(Paths.get(testDirPath.toAbsolutePath().toString(), fileEntity.getName()).toFile()).doesNotExist();

    }


    @AfterEach
    void cleanup() throws IOException {
        FileUtils.deleteDirectory(
                new File(FileStoreUtils.computePathFromRoot(TEST_MAIL, Collections.emptyList()).toString()));
        fileEntityRepository.deleteAll();
        directoryRepository.deleteAll();
        userRepository.deleteAll();

    }
}
