package com.jack.jobportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    private static final String UPLOAD_DIR = "/photos";
    private static final String UPLOAD_PATH_PREFIX = "file:";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = UPLOAD_PATH_PREFIX + Paths.get(UPLOAD_DIR).toAbsolutePath() + "/";
        registry.addResourceHandler(UPLOAD_DIR + "/**").addResourceLocations(uploadPath);
    }
}
