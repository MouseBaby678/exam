package com.baymax.exam.message.feign;

import com.baymax.exam.common.core.exception.ResultException;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.message.MessageResult;
import com.baymax.exam.message.model.MessageInfo;
import com.baymax.exam.user.model.Courses;
import com.baymax.exam.user.model.JoinClass;
import com.baymax.exam.user.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：
 * @modified By：
 * @version:
 */
@FeignClient(value = "exam-message",path = "/message-info")
public interface MessageServiceClient {
    @PostMapping("/inner/send-message")
    Boolean sendMessage(@RequestBody @Validated MessageResult message);
    @PostMapping("/inner/batch/send-message")
    Boolean sendBatchMessage(@RequestBody @Validated MessageResult message);

    @PostMapping("/system/send/classroom")
    Result systemCourseMessage(@RequestBody MessageInfo messageInfo);
}
