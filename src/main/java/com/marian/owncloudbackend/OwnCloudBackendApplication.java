package com.marian.owncloudbackend;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class OwnCloudBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwnCloudBackendApplication.class, args);
    }

}
