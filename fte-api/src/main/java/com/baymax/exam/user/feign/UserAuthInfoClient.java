package com.baymax.exam.user.feign;

import com.baymax.exam.user.model.UserAuthInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：学生信息
 * @modified By：
 * @version:
 */
@FeignClient(value = "exam-user",contextId = "UserAuthInfoClient",path = "/user-auth")
public interface UserAuthInfoClient {
    @GetMapping("/info/{userId}")
    public UserAuthInfo getStudentInfo(@PathVariable Integer userId);
}
