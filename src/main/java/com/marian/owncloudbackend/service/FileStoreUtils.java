package com.marian.owncloudbackend.service;

import com.marian.owncloudbackend.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class FileStoreUtils {

    private static final String DEFAULT_DIR_NAME = "OwnCloud";
    private static final Map<FileType, List<String>> fileTypeMap = Map.of(
            FileType.IMAGE, List.of("image"),
            FileType.VIDEO, List.of("video"),
            FileType.ARCHIVE, List.of("x-freearc",
                    "x-bzip",
                    "x-bzip2",
                    "gzip",
                    "java-archive",
                    "vnd.rar",
                    "x-tar",
                    "zip",
                    "x-7z-compressed"
                    ),
            FileType.PDF,List.of("pdf")
    );

    public static void makeBaseDir() {
        File userDirectory = FileUtils.getUserDirectory();
        Path defaultDirectoryPath = Path.of(userDirectory.getAbsolutePath(), DEFAULT_DIR_NAME);
        File defaultDirectory = new File(defaultDirectoryPath.toString());
        if (!defaultDirectory.exists()) {
            boolean creationSuccess = defaultDirectory.mkdir();
            if (creationSuccess) {
                log.info("Default dir created successfully");
            } else log.error("Default dir creation error");
            return;
        }

        log.info("Default dir has been found successfully");


    }

    public static String getBaseDir() {
        File userDirectory = FileUtils.getUserDirectory();
        String absolutePath = userDirectory.getAbsolutePath();
        return Paths.get(absolutePath, DEFAULT_DIR_NAME).toString();
    }

    public static FileType getFileTypeFromContentType(String contentType) {
        for (Map.Entry<FileType, List<String>> fileTypeListEntry : fileTypeMap.entrySet()) {
            List<String> contentTypeList = fileTypeListEntry.getValue();
            for (String ctype : contentTypeList) {
                if (contentType.contains(ctype)) return fileTypeListEntry.getKey();
            }
        }

        return FileType.FILE;
    }


    public static Path computePathFromRoot(String email, List<String> pathsFromRoot) {
        List<String> fullPath = new LinkedList<>();
        fullPath.add(email);
        fullPath.addAll(pathsFromRoot);
        return Path.of(FileStoreUtils.getBaseDir(),fullPath.toArray(new String[0]));
    }

}
