package com.ldz.park;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//用于配置 MyBatis 在 Spring 或 Spring Boot 项目中扫描 Mapper 接口的位置

@MapperScan("com.ldz.park.dao")
public class ParkApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =   SpringApplication.run(ParkApplication.class, args);
    }

}
