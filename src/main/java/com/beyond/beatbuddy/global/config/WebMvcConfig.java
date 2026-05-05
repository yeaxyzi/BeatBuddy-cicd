package com.beyond.beatbuddy.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.profile}")
    private String profileUploadDir;

    @Value("${file.upload.group}")
    private String groupUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String profilePath = new File(profileUploadDir).getAbsoluteFile().toURI().toString();
        String groupPath = new File(groupUploadDir).getAbsoluteFile().toURI().toString();

        registry.addResourceHandler("/images/profiles/**")
                .addResourceLocations(profilePath);

        registry.addResourceHandler("/images/groups/**")
                .addResourceLocations(groupPath);
    }
}
