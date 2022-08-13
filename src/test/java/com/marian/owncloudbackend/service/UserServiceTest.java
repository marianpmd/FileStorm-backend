package com.marian.owncloudbackend.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.mapper.UserMapper;
import com.marian.owncloudbackend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getTotalAssignedSpace() {
        //Arrange - setup our mocks and provide expectations.
        UserEntity expected1 = new UserEntity("test1@test.co", "abc", "user");
        UserEntity expected2 = new UserEntity("test2@test.co", "abc", "user");
        expected1.setAssignedSpace(BigInteger.TEN);
        expected2.setAssignedSpace(BigInteger.TEN);
        long expectedSum = 20L;
        when(userRepository.findAll()).thenReturn(List.of(expected1,expected2));

        //Act - call the method
        long actualSum = userService.getTotalAssignedSpace();

        //Assert - write our assertions based on expectations
        assertEquals(actualSum,expectedSum);

    }

}