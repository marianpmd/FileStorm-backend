package com.marian.owncloudbackend;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
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
            if (!userService.existsByEmail(ADMIN_EMAIL)){
                ListIterator<String> argIterator = List.of(args).listIterator();
                while (argIterator.hasNext()) {
                    var arg = argIterator.next();
                    if (arg.equals("create-admin")) {
                        var adminPassword = argIterator.next();
                        if (!StringUtils.isEmpty(adminPassword)) {
                            UserEntity admin = userService.registerNewUser(ADMIN_EMAIL, adminPassword, "admin");
                            boolean wasSuccessful = this.fileStoreService.createUserDirectory(admin);
                            if (wasSuccessful){
                                log.info("Created admin dir successfully");
                            }else {
                                log.error("Creation of admin dir for admin {} failed",admin);
                            }
                        }
                    }
                }

            }

        }
        while(true){
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            System.out.println("NEW-DEP *****************************");
            Thread.sleep(500L);
        }
    }
}
