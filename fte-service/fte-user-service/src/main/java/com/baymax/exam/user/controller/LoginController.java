package com.baymax.exam.user.controller;


import com.baymax.exam.common.core.exception.ResultException;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.common.redis.utils.RedisUtils;
import com.baymax.exam.user.service.impl.LoginServiceImpl;
import com.baymax.exam.user.model.User;
import com.baymax.exam.user.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Baymax
 * @since 2022-03-12
 */
@Slf4j
@Tag( name="登录模块")
@RestController
@RequestMapping("/public/user")
@Validated
public class LoginController {
    @Autowired
    LoginServiceImpl loginService;
    @Autowired
    UserServiceImpl userService;
    @Autowired
    RedisUtils redisUtils;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result register(@RequestBody @Validated(User.RegisterRequestValid.class) User registerRequest,
                           @RequestParam String code){
        String email = registerRequest.getEmail();
        String redisEmailCodeKey = loginService.getRedisEmailCodeKey(email);
        if(redisUtils.hasKey(redisEmailCodeKey)){
            Integer emailCode=redisUtils.getCacheObject(redisEmailCodeKey);
            log.info("验证码：{}",emailCode);
            if(code.equals(emailCode.toString())){
                redisUtils.deleteObject(redisEmailCodeKey);
                return userService.addUser(registerRequest);
            }
        }
        return Result.msgError("验证码不正确");
    }

    @Operation(summary = "找回密码")
    @PostMapping("/forgetPass")
    public Result forgetPass(@RequestParam String email,
                             @RequestParam String code,
                             @RequestBody String password){
         String redisEmailCodeKey = loginService.getRedisEmailCodeKey(email);
        Integer emailCode = redisUtils.getCacheObject(redisEmailCodeKey);
        if(code.equals(emailCode.toString())){
            User userByEmail = userService.getUserByEmail(email);
            userService.updatePassword(userByEmail.getId(),password);
            redisUtils.deleteObject(redisEmailCodeKey);
            return Result.msgSuccess("修改成功");
        }
        return Result.msgSuccess("验证码不正确");
    }
    @Operation(summary = "发送验证码")
    @PostMapping("/sendEmailCode")
    public Result sendEmailCode(@RequestParam String type,
                                @RequestParam String email) throws ResultException {
        User userByEmail = userService.getUserByEmail(email);
        if("forget".equals(type)){
            if(userByEmail==null){
                return Result.msgInfo("邮箱不存在");
            }
        }else if("register".equals(type)){
            if(userByEmail!=null){
                return Result.msgInfo("邮箱已注册");
            }
        }else {
            return Result.msgError("方式不存在");
        }
        return loginService.sendEmailCode(email);
    }
}
