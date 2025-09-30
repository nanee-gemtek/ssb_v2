package com.mysite.sbb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.docs-dir}") // 문서 저장 루트 (예: /var/www/uploadDocs)
    private String docsDir;

    @Value("${file.upload-study-dir}")
    private String uploadStudyDir; // 설치 사례 이미지 저장 루트

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/** 요청을 uploadDir 실제 경로로 매핑
        registry.addResourceHandler("/uploadImages/**")
                .addResourceLocations("file:///" + uploadDir + "/"); // 실제 경로
        registry.addResourceHandler("/uploadStudyImages/**")
                .addResourceLocations("file:///" + uploadStudyDir + "/"); // 실제 경로
        registry.addResourceHandler("/uploadDocs/**")
                .addResourceLocations("file:///" + docsDir  + "/"); // 실제 경로
    }
}