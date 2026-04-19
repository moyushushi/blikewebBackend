package com.blike.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 读取你配置的本地路径 D:/blike-upload/
    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 封面映射
        registry.addResourceHandler("/upload/cover/**")
                .addResourceLocations("file:" + uploadPath + "cover/")
                .setCacheControl(CacheControl.noCache());

        // =======================
        // 视频：解决无限请求
        // =======================
        registry.addResourceHandler("/upload/video/**")
                .addResourceLocations("file:" + uploadPath + "video/")
                .setCacheControl(CacheControl.noCache())
                .resourceChain(true)  // 关键：开启资源链
                .addResolver(new PathResourceResolver()); // 关键：解析本地文件

        // 头像映射
        registry.addResourceHandler("/upload/avatar/**")
                .addResourceLocations("file:" + uploadPath + "avatar/")
                .setCacheControl(CacheControl.noCache());
    }
}