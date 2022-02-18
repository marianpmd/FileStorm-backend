package com.marian.owncloudbackend.controller;

import com.marian.owncloudbackend.DTO.UserDTO;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.service.FileStoreService;
import com.marian.owncloudbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FileStoreService fileStoreService;

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        List<UserDTO> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(String email,String password){
        UserDTO userDTO = userService.registerNewUser(email, password);

        boolean wasSuccessful = this.fileStoreService.createUserDirectory(userDTO);


        return ResponseEntity.ok(userDTO);

    }


}
