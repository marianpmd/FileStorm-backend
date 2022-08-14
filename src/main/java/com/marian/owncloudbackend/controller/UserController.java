package com.marian.owncloudbackend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marian.owncloudbackend.dto.AssignRequestDTO;
import com.marian.owncloudbackend.dto.SystemInfoDTO;
import com.marian.owncloudbackend.dto.UserAuthDTO;
import com.marian.owncloudbackend.dto.UserDTO;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.service.DirectoryService;
import com.marian.owncloudbackend.service.FileStoreService;
import com.marian.owncloudbackend.service.NotificationService;
import com.marian.owncloudbackend.service.UserService;

import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<Object> getUserInfo(String email) {
        String authenticatedUserEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        if (!authenticatedUserEmail.equals(email)) {
            return new ResponseEntity<>("Not allowed", HttpStatus.FORBIDDEN);
        }
        UserDTO userDTObyEmail = userService.getUserDTObyEmail(email);
        return ResponseEntity.ok(userDTObyEmail);
    }

    @PostMapping("/register")
    public ResponseEntity<UserAuthDTO> registerNewUser(@RequestBody UserAuthDTO user) {
        UserEntity userEntity = userService.registerNewUser(user.email(), user.password(), "user");
        this.fileStoreService.createUserDirectory(userEntity);
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
        Long usableSpace = userService.getSystemInfo().usableSpace();
        UserDTO userDTO = userService.assignToUser(userId, assignRequest.amount(), usableSpace);

        notificationService.notifyUserForAssignment(userDTO, assignRequest.amount(), assignRequest.description());

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/systemInfo")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<SystemInfoDTO> getSystemInfo() {
        SystemInfoDTO systemInfo = userService.getSystemInfo();
        return ResponseEntity.ok(systemInfo);
    }


}
