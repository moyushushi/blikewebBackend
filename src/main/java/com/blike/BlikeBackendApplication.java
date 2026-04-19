package com.blike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blike.mapper")   // 扫描 mapper 接口所在的包
public class BlikeBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlikeBackendApplication.class, args);
        System.out.println("success");
    }
}