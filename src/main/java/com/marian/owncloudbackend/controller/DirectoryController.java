package com.marian.owncloudbackend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marian.owncloudbackend.DTO.DirectoriesWithParentDTO;
import com.marian.owncloudbackend.DTO.DirectoryDTO;
import com.marian.owncloudbackend.service.DirectoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dir")
@RequiredArgsConstructor
public class DirectoryController {
    private final DirectoryService directoryService;

    @PostMapping("/create")
    public ResponseEntity<DirectoryDTO> createDirectory(@RequestBody ArrayList<String> pathsFromRoot) {
        var directory = directoryService.createDirectory(pathsFromRoot);
        return ResponseEntity.ok(directory);
    }

    @PostMapping("/getAll")
    public ResponseEntity<DirectoriesWithParentDTO> getAllDirectoriesInPath(@RequestBody ArrayList<String> pathsFromRoot){
        DirectoriesWithParentDTO allDirectoriesInPath = directoryService.getAllDirectoriesInPath(pathsFromRoot);
        return ResponseEntity.ok(allDirectoriesInPath);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteDirById(@RequestParam Long id) throws IOException {
        DirectoryDTO directoryDTO = directoryService.deleteDirById(id);
        return ResponseEntity.ok(directoryDTO);
    }

}
