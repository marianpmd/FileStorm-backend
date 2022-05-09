//package com.marian.owncloudbackend.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.AsyncTaskExecutor;
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Configuration
//@EnableAsync
//@Slf4j
//public class AsyncConfiguration implements WebMvcConfigurer {
//
//    @Override
//    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//        configurer.setDefaultTimeout(-1);
//        configurer.setTaskExecutor(asyncTaskExecutor());
//    }
//
//    @Bean
//    public AsyncTaskExecutor asyncTaskExecutor() {
//        return new SimpleAsyncTaskExecutor("async");
//    }
//}