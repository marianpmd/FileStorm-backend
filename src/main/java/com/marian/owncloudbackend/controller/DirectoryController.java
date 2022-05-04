package com.marian.owncloudbackend.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<DirectoryDTO>> getAllDirectoriesInPath(@RequestBody ArrayList<String> pathsFromRoot){
        List<DirectoryDTO> allDirectoriesInPath = directoryService.getAllDirectoriesInPath(pathsFromRoot);
        return ResponseEntity.ok(allDirectoriesInPath);
    }

}