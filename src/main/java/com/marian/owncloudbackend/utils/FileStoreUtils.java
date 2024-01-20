package com.marian.owncloudbackend.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.marian.owncloudbackend.enums.FileType;
import com.marian.owncloudbackend.exceptions.AbnormalAssignmentAmountException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileStoreUtils {

    private FileStoreUtils() {
    }

    public static final String DEFAULT_DIR_NAME = "OwnCloud";
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
            FileType.PDF, List.of("pdf")
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
            for (String cType : contentTypeList) {
                if (contentType.contains(cType)) return fileTypeListEntry.getKey();
            }
        }

        return FileType.FILE;
    }

    public static Path computePathFromRoot(String email, List<String> pathsFromRoot) {
        List<String> fullPath = new LinkedList<>();
        fullPath.add(email);
        fullPath.addAll(sanitizePaths(pathsFromRoot));
        return Path.of(FileStoreUtils.getBaseDir(), fullPath.toArray(new String[0]));
    }

    private static Collection<String> sanitizePaths(List<String> pathsFromRoot) {
        if (CollectionUtils.isEmpty(pathsFromRoot)) return Collections.emptyList();
        return pathsFromRoot.stream()
                .map(StringUtils::deleteWhitespace)
                .toList();
    }

    public static long parseAmountString(String amount) {
        String[] split = amount.split(" ");
        String value = split[0];
        String unit = split[1];
        return switch (amount) {
            case "500 MB","100GB","50GB", "20 GB", "10 GB", "5 GB", "2 GB", "1 GB" -> getAmountFromValueAndUnit(value, unit);
            default -> throw new AbnormalAssignmentAmountException("Assignment amount :" + amount + " is not " +
                    "allowed");
        };
    }

    private static long getAmountFromValueAndUnit(String value, String unit) {
        switch (unit) {
            case "MB" -> {
                var mbToB = 1_000_000L;
                return Long.parseLong(value) * mbToB;
            }
            case "GB" -> {
                var gbToB = 1_000_000_000L;
                return Long.parseLong(value) * gbToB;
            }
            default -> throw new AbnormalAssignmentAmountException("unit unknown");
        }
    }
}
