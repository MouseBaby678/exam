package com.baymax.exam.user.feign;

import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.user.model.Classes;
import com.baymax.exam.user.model.Courses;
import com.baymax.exam.user.model.JoinClass;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：
 * @modified By：
 * @version:
 */
/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：
 * @modified By：
 * @version:
 */
@FeignClient(value = "exam-user",contextId = "ClassesClient",path = "/classes")
public interface ClassesClient {
    @GetMapping("/info/{classId}")
    public Result<Classes> classInfo(@PathVariable String classId);

    @PostMapping("/{courseId}/part/list")
    public Result<List<Classes>> getClassListByIds(
            @RequestBody Collection<Integer> classIds,
            @PathVariable Integer courseId
    );
    @PostMapping("/{courseId}/user/class")
    public Classes getClassByUserId(
            @PathVariable Integer courseId,
            @RequestParam Integer userId);
}
