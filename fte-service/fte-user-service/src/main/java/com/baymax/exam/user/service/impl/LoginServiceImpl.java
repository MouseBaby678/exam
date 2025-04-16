package com.baymax.exam.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baymax.exam.common.core.base.RedisKeyConstants;
import com.baymax.exam.common.core.exception.ResultException;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.common.redis.utils.RedisUtils;
import com.baymax.exam.mails.feign.MailsServiceClient;
import com.baymax.exam.mails.model.Mails;
import com.baymax.exam.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Baymax
 * @since 2022-03-12
 */
@Service
@Slf4j
public class LoginServiceImpl {
    @Autowired
    UserServiceImpl userService;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    MailsServiceClient mailsServiceClient;

    @Value("${exam.email.interval}")
    int interval;
    @Value("${exam.email.aging}")
    int emailAging;

    @Value("${exam.front.host}")
    String host;

    public String getRedisEmailCodeKey(String email){
        return RedisKeyConstants.REDIS_EMAIL_CODE_KEY+email;
    }

    /**
     * 发送验证码
     * @param email
     * @return
     */
    public Result sendEmailCode(String email) throws ResultException {
        String redisEmailCodeKey = getRedisEmailCodeKey(email);
        if(redisUtils.hasKey(redisEmailCodeKey)){
            return Result.msgInfo("验证码已发送，请稍后再试~");
        }
        Integer code= RandomUtil.randomInt(1000,9999);
        Mails mails=new Mails();
        mails.setTo(email);
        mails.setSubject("考试系统验证码");
        mails.setText("验证码为："+code);
        Result result = mailsServiceClient.sendMail(mails);
        if(Result.isSuccess(result)){
            redisUtils.setCacheObject(redisEmailCodeKey, code,emailAging, TimeUnit.SECONDS);
            return Result.msgSuccess("验证码发送成功，请尽快完成注册~");
        }else{
            return Result.msgError("验证码发送失败~");
        }
    }
}
