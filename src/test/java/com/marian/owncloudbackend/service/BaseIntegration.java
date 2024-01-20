package com.marian.owncloudbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.repository.UserRepository;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BaseIntegration {


    @Autowired
    protected MockMvc mvc;

    public static final String TEST_MAIL = "user@user.com";

    public static String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UserEntity createUserWithDefaultDirectory(UserRepository userRepository, FileStoreService fileStoreService) {
        TestSecurityContextHolder.setAuthentication(new UsernamePasswordAuthenticationToken(TEST_MAIL, null, null));
        UserEntity userEntity = userRepository.save(new UserEntity(TEST_MAIL, null, "user"));
        fileStoreService.createUserDirectory(userEntity);

        return userEntity;
    }

}
