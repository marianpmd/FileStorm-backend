package com.marian.owncloudbackend;

import com.marian.owncloudbackend.utils.FileStoreUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
@EnableAsync
public class FileStormBackendApplication {

    public static void main(String[] args) {
        System.out.println("STARTING BACKEND APP");
        SpringApplication.run(FileStormBackendApplication.class, args);
        FileStoreUtils.makeBaseDir();
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:4200","https://mariancr.go.ro","http://192.168.1.108"));
        config.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Origin","Origin","ngsw-bypass", "Content-Type", "Accept","Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
