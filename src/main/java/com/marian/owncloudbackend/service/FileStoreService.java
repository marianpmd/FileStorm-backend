package com.marian.owncloudbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;

@Service
@Slf4j
public class FileStoreService {

    private static final String DEFAULT_DIR_NAME = "OwnCloud";

    public void makeBaseDir() {
        File userDirectory = FileUtils.getUserDirectory();
        Path defaultDirectoryPath = Path.of(userDirectory.getAbsolutePath(),DEFAULT_DIR_NAME);
        File defaultDirectory = new File(defaultDirectoryPath.toString());
        if (!defaultDirectory.exists()) {
            boolean creationSuccess = defaultDirectory.mkdir();
            if (creationSuccess){
                log.info("Default dir created successfully");
            }else log.error("Default dir creation error");
            return;
        }

        log.info("Default dir has been found successfully");


    }
}
