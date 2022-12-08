package com.marian.owncloudbackend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaTray;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.marian.owncloudbackend.dto.FileEntityDTO;
import com.marian.owncloudbackend.dto.SystemInfoDTO;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.service.FileStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileStoreService fileStoreService;

    @PostMapping("/upload")
    public ResponseEntity<FileEntityDTO> uploadFile(final MultipartFile file,
                                                    @RequestParam final List<String> pathFromRoot,
                                                    @RequestParam(required = false) final boolean shouldUpdate) throws IOException {
        log.info("New file to be uploaded : {}", file.getOriginalFilename());

        FileEntityDTO fileEntityDTO = fileStoreService.uploadNewFile(file, pathFromRoot, shouldUpdate);

        return ResponseEntity.ok(fileEntityDTO);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFile(@RequestParam List<String> pathFromRoot,
                                             @RequestParam String filename) {
        if (fileStoreService.checkIfExists(filename, pathFromRoot)) return ResponseEntity.ok().body(true);
        return ResponseEntity.ok().body(false);

    }

    @GetMapping("/all")
    public ResponseEntity<Page<FileEntityDTO>> getAllFilesForUser(String sortBy,
                                                                  int page,
                                                                  int size,
                                                                  boolean asc,
                                                                  @RequestParam List<String> pathFromRoot) {
        var userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Page<FileEntityDTO> allFilesForUser = fileStoreService
                .getAllFilesForUser(userEmail, sortBy, page, size, asc, pathFromRoot);

        return ResponseEntity.ok(allFilesForUser);

    }

    @GetMapping("/one")
    public StreamingResponseBody getFileFromUserAndId(HttpServletResponse response, @RequestParam Long id) {
        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        File file = fileStoreService.getFileByIdAndUser(id, userEmail);
        String fileName = file.getName();

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        return outputStream -> {
            FileInputStream inputStream = new FileInputStream(file);
            IOUtils.copyLarge(inputStream, response.getOutputStream());
            inputStream.close();
        };
    }

    @GetMapping("/public")
    public StreamingResponseBody getPublicFile(HttpServletResponse response, @RequestParam Long id) {
        File file = fileStoreService.getFileByIdPublic(id);
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
        String userEmail = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (fileStoreService.deleteFileByIdAndUser(id, userEmail)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body("File was not deleted");
    }

    @GetMapping("/byKeyword")
    public ResponseEntity<List<FileEntityDTO>> findAllLike(String keyword) {
        List<FileEntityDTO> allFilesLike = fileStoreService.getAllFilesLike(keyword);

        return ResponseEntity.ok(allFilesLike);
    }


    @PutMapping("/makePublic")
    public ResponseEntity<FileEntityDTO> makeFilePublic(Long id) {
        FileEntityDTO fileEntityDTO = fileStoreService.makeFilePublic(id);
        return ResponseEntity.ok(fileEntityDTO);
    }

    @PutMapping("/makePrivate")
    public ResponseEntity<FileEntityDTO> makeFilePrivate(Long id) {
        FileEntityDTO fileEntityDTO = fileStoreService.makeFilePrivate(id);
        return ResponseEntity.ok(fileEntityDTO);
    }

    @GetMapping(value = "/thumbnail",
    produces = IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getFileThumbnail(Long id){
        return fileStoreService.getThumbnailForFile(id);
    }

}
