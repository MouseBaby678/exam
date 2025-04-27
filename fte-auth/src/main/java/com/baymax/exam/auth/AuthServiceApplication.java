package com.baymax.exam.auth;

import com.baymax.exam.user.feign.UserClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 21:56
 * @description：认证服务器
 * @modified By：
 * @version:
 */
@EnableFeignClients(basePackageClasses = UserClient.class)
@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class,args);
    }
}
