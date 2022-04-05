package com.marian.owncloudbackend.controller;

import com.marian.owncloudbackend.DTO.FileEntityDTO;
import com.marian.owncloudbackend.service.FileStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileStoreService fileStoreService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(final MultipartFile file) throws IOException {
        log.info("New file to be uploaded : {}", file);

        FileEntityDTO fileEntityDTO = fileStoreService.uploadNewFile(file);

        return ResponseEntity.ok(fileEntityDTO);
    }

    @GetMapping("/all")
    public ResponseEntity<List<FileEntityDTO>> getAllFilesForUser(){
        var userEmail =(String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<FileEntityDTO> allFilesForUser = fileStoreService.getAllFilesForUser(userEmail);

        return ResponseEntity.ok(allFilesForUser);

    }

}
