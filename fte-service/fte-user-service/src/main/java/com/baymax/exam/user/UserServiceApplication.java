package com.baymax.exam.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author MouseBaby678
 * @date ：Created in 2025/4/27 22:14
 * @description：
 * @modified By：
 * @version:
 */
@SpringBootApplication(scanBasePackages = "com.baymax.exam")
@EnableFeignClients(basePackages="com.baymax.exam.*")
//扫描mapper
@MapperScan(basePackages = "com.baymax.exam.user.mapper")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
