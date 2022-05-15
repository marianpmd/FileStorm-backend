package com.marian.owncloudbackend.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.marian.owncloudbackend.enums.FileType;
import com.marian.owncloudbackend.exceptions.AbnormalAssignmentAmountException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileStoreUtils {

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
        return Path.of(FileStoreUtils.getBaseDir(), fullPath.toArray(new String[0]));
    }

    public static long parseAmountString(String amount) {
        String[] split = amount.split(" ");
        String value = split[0];
        String unit = split[1];
        return switch (amount) {
            case "500 MB", "20 GB", "10 GB", "5 GB", "2 GB", "1 GB" -> getAmountFromValueAndUnit(value, unit);
            default -> throw new AbnormalAssignmentAmountException("Assignment amount :" + amount + " is not " +
                    "allowed");
        };
    }

    private static long getAmountFromValueAndUnit(String value, String unit) {
        switch (unit) {
            case "MB" -> {
                var RATIO_MB_B =1_000_000L;
                return Long.parseLong(value) * RATIO_MB_B;
            }
            case "GB" -> {
                var RATIO_GB_B = 1_000_000_000L;
                return Long.parseLong(value) * RATIO_GB_B;
            }
            default -> throw new AbnormalAssignmentAmountException("unit unknown");
        }
    }
}
