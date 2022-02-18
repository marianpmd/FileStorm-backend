package com.marian.owncloudbackend.controller;

import com.marian.owncloudbackend.service.FileStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.file.PathUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileStoreService fileStoreService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(final MultipartFile file) throws IOException {
        log.info("New file to be uploaded : {}" , file);

        fileStoreService.uploadNewFile(file);

        return ResponseEntity.ok("");
    }
}
