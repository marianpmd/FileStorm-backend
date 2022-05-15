package com.marian.owncloudbackend.controller;

import com.marian.owncloudbackend.DTO.AssignRequestDTO;
import com.marian.owncloudbackend.DTO.UserAuthDTO;
import com.marian.owncloudbackend.DTO.UserDTO;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.service.DirectoryService;
import com.marian.owncloudbackend.service.FileStoreService;
import com.marian.owncloudbackend.service.NotificationService;
import com.marian.owncloudbackend.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final DirectoryService directoryService;
    private final FileStoreService fileStoreService;
    private final NotificationService notificationService;

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(String email){
        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        if (!authenticatedUserEmail.equals(email)){
            return new ResponseEntity<>("Not allowed", HttpStatus.FORBIDDEN);
        }
        UserDTO userDTObyEmail = userService.getUserDTObyEmail(email);
        return ResponseEntity.ok(userDTObyEmail);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(@RequestBody UserAuthDTO user) {
        UserEntity userEntity = userService.registerNewUser(user.email(), user.password(),"user");
        boolean wasSuccessful = this.fileStoreService.createUserDirectory(userEntity);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/delete")
    public ResponseEntity<UserDTO> deleteUserById(@RequestParam Long userId) throws IOException {
        UserDTO userDTO = directoryService.deleteUserById(userId);

        return ResponseEntity.ok(userDTO);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/assign")
    public ResponseEntity<UserDTO> assignToUser(
            @RequestParam Long userId,
            @RequestBody AssignRequestDTO assignRequest
    ) {
        Long usableSpace = fileStoreService.getSystemInfo().usableSpace();
        UserDTO userDTO = userService.assignToUser(userId, assignRequest.amount(),usableSpace);

        notificationService.notifyUserForAssignment(userDTO, assignRequest.amount(), assignRequest.description());

        return ResponseEntity.ok(userDTO);
    }


}
