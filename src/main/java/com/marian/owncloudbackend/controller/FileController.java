package com.marian.owncloudbackend.controller;

import com.marian.owncloudbackend.DTO.FileEntityDTO;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.service.FileStoreService;
import com.marian.owncloudbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileStoreService fileStoreService;
    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<FileEntityDTO> uploadFile(final MultipartFile file,
                                                    @RequestParam(required = false) final Boolean shouldUpdate) throws IOException {
        log.info("New file to be uploaded : {}", file.getOriginalFilename());

        FileEntityDTO fileEntityDTO = fileStoreService.uploadNewFile(file, shouldUpdate);

        return ResponseEntity.ok(fileEntityDTO);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFile(String filename) {
        boolean fileExists = fileStoreService.checkIfExists(filename);
        if (fileExists) {
            return ResponseEntity.ok().body(true);
        }

        return ResponseEntity.ok().body(false);

    }

    @GetMapping("/all")
    public ResponseEntity<Page<FileEntityDTO>> getAllFilesForUser(String sortBy, int page, int size, boolean asc) {
        var userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Page<FileEntityDTO> allFilesForUser = fileStoreService.getAllFilesForUser(userEmail, sortBy, page, size, asc);

        return ResponseEntity.ok(allFilesForUser);

    }

    @GetMapping("/one")
    public StreamingResponseBody getFileFromUserAndId(HttpServletResponse response, @RequestParam Long id) {
        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);
        File file = fileStoreService.getFileByIdAndUser(id, userByEmail);
        String fileName = file.getName();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        return outputStream -> {
            FileInputStream inputStream = new FileInputStream(file);
            IOUtils.copyLarge(inputStream, response.getOutputStream());
            inputStream.close();
        };
    }

    @DeleteMapping("/delete/one")
    public ResponseEntity<String> deleteFileFromUserAndId(@RequestParam Long id) {
        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);

        if (fileStoreService.deleteFileByIdAndUser(id, userByEmail)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body("File was not deleted");
    }

}
