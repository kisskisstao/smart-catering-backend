package com.nuit.yujin.smartcateringbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@MapperScan("com.nuit.yujin.smartcateringbackend.mapper")

public class SmartCateringBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCateringBackendApplication.class, args);
    }

}
