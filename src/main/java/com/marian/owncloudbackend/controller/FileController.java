package com.marian.owncloudbackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(final MultipartFile file){
        log.info("New file to be uploaded : {}" , file);

        return ResponseEntity.ok("");
    }
}
