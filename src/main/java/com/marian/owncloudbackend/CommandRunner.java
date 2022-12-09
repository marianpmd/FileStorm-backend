package com.marian.owncloudbackend;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.service.FileStoreService;
import com.marian.owncloudbackend.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
@RequiredArgsConstructor
public class CommandRunner implements CommandLineRunner {

    public static final String ADMIN_EMAIL = "admin@admin.com";
    private final UserService userService;
    private final FileStoreService fileStoreService;

    @Override
    public void run(String... args) throws Exception {
        if (!ArrayUtils.isEmpty(args)) {
            processArgsForAdmin(args);
            processArgsForThumbnailResync(args);
        }
    }

    private void processArgsForThumbnailResync(String[] args) {
        Arrays.stream(args)
                .forEach(arg -> {
                    if (arg.equals("resync-thumbnails")) {
                        log.info("PROCESSING ALL FILES IN ORDER TO RECREATE THE THUMBNAILS");
                        resyncThumbnails();
                    }
                });
    }

    private void processArgsForAdmin(String[] args) {
        if (!userService.existsByEmail(ADMIN_EMAIL)) {
            ListIterator<String> argIterator = List.of(args).listIterator();
            while (argIterator.hasNext()) {
                var arg = argIterator.next();
                if (arg.equals("create-admin")) {
                    var adminPassword = argIterator.next();
                    createAdmin(adminPassword);
                }
            }

        }
    }

    private void resyncThumbnails() {
        boolean success = fileStoreService.resyncAllThumbnails();
        if (!success) {
            log.info("THUMBNAIL RESYNC FAILED");
        }

        log.info("THUMBNAIL RESYNC SUCCESSFUL");
    }

    private void createAdmin(String adminPassword) {
        if (!StringUtils.isEmpty(adminPassword)) {
            UserEntity admin = userService.registerNewUser(ADMIN_EMAIL, adminPassword, "admin");
            boolean wasSuccessful = this.fileStoreService.createUserDirectory(admin);
            if (wasSuccessful) {
                log.info("Created admin dir successfully");
            } else {
                log.error("Creation of admin dir for admin {} failed", admin);
            }
        }
    }
}
