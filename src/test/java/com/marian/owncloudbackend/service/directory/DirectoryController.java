package com.marian.owncloudbackend.service.directory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.repository.DirectoryRepository;
import com.marian.owncloudbackend.repository.UserRepository;
import com.marian.owncloudbackend.service.BaseIntegration;
import com.marian.owncloudbackend.service.FileStoreService;
import com.marian.owncloudbackend.utils.FileStoreUtils;

class DirectoryControllerTest extends BaseIntegration {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private DirectoryRepository directoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void createDirectory() throws Exception {
        //Arrange
        createUserWithDefaultDirectory(userRepository, fileStoreService);
        Path testPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("new_dir"));

        //Act
        MvcResult response = mvc.perform(post("/dir/create").contentType(MediaType.APPLICATION_JSON).content(asJsonString(List.of("new_dir")))).andDo(print()).andExpect(status().isOk()).andReturn();
        JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());
        long directoryId = jsonNode.get("id").asLong();
        String directoryPath = jsonNode.get("path").asText();
        String directoryName = jsonNode.get("name").asText();
        DirectoryEntity savedDirectoryEntity = directoryRepository.findById(directoryId).orElseThrow();

        //Assert
        assertThat(testPath).exists();

        assertThat(savedDirectoryEntity.getId()).isEqualTo(directoryId);
        assertThat(savedDirectoryEntity.getPath()).isEqualTo(directoryPath);
        assertThat(savedDirectoryEntity.getName()).isEqualTo(directoryName);

    }

    @Test
    void getAllDirectoriesInPath() throws Exception {
        //Arrange
        UserEntity userEntity = createUserWithDefaultDirectory(userRepository, fileStoreService);
        Path testPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("new_dir"));
        Path testInnerDirPath1 = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("new_dir", "inner1"));
        Path testInnerDirPath2 = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("new_dir", "inner2"));
        this.directoryRepository.save(new DirectoryEntity(testPath.toAbsolutePath().toString(), "new_dir", userEntity));
        this.directoryRepository.save(new DirectoryEntity(testInnerDirPath1.toAbsolutePath().toString(), "inner1", userEntity));
        this.directoryRepository.save(new DirectoryEntity(testInnerDirPath2.toAbsolutePath().toString(), "inner2", userEntity));
        FileUtils.forceMkdir(testPath.toFile());
        FileUtils.forceMkdir(testInnerDirPath1.toFile());
        FileUtils.forceMkdir(testInnerDirPath2.toFile());

        //Act
        MvcResult response = mvc.perform(get("/dir/getAll/inPath/new_dir").contentType(MediaType.APPLICATION_JSON).content(asJsonString(List.of("new_dir")))).andDo(print()).andExpect(status().isOk()).andReturn();

        JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());
        String parentPath = jsonNode.get("parent").get("path").asText();
        Iterator<JsonNode> directories = jsonNode.get("directories").elements();
        JsonNode element1 = directories.next();
        JsonNode element2 = directories.next();

        String inner1Path = element1.get("path").asText();
        String inner2Path = element2.get("path").asText();

        String inner1Name = element1.get("name").asText();
        String inner2Name = element2.get("name").asText();


        //Assert
        assertThat(testPath).exists();
        assertThat(testInnerDirPath1).exists();
        assertThat(testInnerDirPath2).exists();
        assertThat(parentPath).isEqualTo(testPath.getParent().toAbsolutePath().toString());
        assertThat(inner1Path).isEqualTo(testInnerDirPath1.toAbsolutePath().toString());
        assertThat(inner2Path).isEqualTo(testInnerDirPath2.toAbsolutePath().toString());
        assertThat(inner1Name).isEqualTo("inner1");
        assertThat(inner2Name).isEqualTo("inner2");
    }

    @Test
    void deleteDirById() throws Exception {
        //Arrange
        UserEntity userEntity = createUserWithDefaultDirectory(userRepository, fileStoreService);
        userEntity.setOccupiedSpace(new BigInteger("100"));
        userEntity.setAssignedSpace(new BigInteger("100"));
        userRepository.save(userEntity);
        Path testPath = FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("new_dir"));
        File file = new File(FileStoreUtils.computePathFromRoot(TEST_MAIL, List.of("new_dir", "innerFile")).toUri());
        FileUtils.touch(file);
        DirectoryEntity newDir = this.directoryRepository.save(new DirectoryEntity(testPath.toAbsolutePath().toString(), "new_dir", userEntity));
        FileUtils.forceMkdir(testPath.toFile());

        //Act
        MvcResult response = mvc.perform(delete("/dir/delete" + "?id=" + newDir.getId()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();

        JsonNode jsonNode = objectMapper.readTree(response.getResponse().getContentAsString());
        long dirId = jsonNode.get("id").asLong();
        String dirPath = jsonNode.get("path").asText();
        String dirName = jsonNode.get("name").asText();
        UserEntity reallocatedUser = userRepository.findById(userEntity.getId()).orElseThrow();
        boolean exists = directoryRepository.existsById(dirId);

        //Assert
        assertThat(testPath).doesNotExist();
        assertThat(file).doesNotExist();
        assertThat(dirId).isEqualTo(newDir.getId());
        assertThat(dirPath).isEqualTo(newDir.getPath());
        assertThat(dirName).isEqualTo(newDir.getName());
        assertThat(reallocatedUser.getOccupiedSpace()).isEqualTo(BigInteger.ZERO);
        assertThat(exists).isFalse();


    }


    @AfterEach
    void clearDirs() throws IOException {
        FileUtils.deleteDirectory(new File(FileStoreUtils.computePathFromRoot(TEST_MAIL, Collections.emptyList()).toString()));
        directoryRepository.deleteAll();
        userRepository.deleteAll();

    }


}
