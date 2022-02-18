package com.marian.owncloudbackend;

import com.marian.owncloudbackend.service.FileStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandRunner implements CommandLineRunner {

    private final FileStoreService fileStoreService;

    @Override
    public void run(String... args) throws Exception {
        this.fileStoreService.makeBaseDir();
    }
}
