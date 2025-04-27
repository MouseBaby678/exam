package com.baymax.exam.user.feign;

import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.user.model.Courses;
import com.baymax.exam.user.model.JoinClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：
 * @modified By：
 * @version:
 */
@FeignClient(value = "exam-user",contextId = "CourseClient",path = "/courses")
public interface CourseClient {
    @GetMapping("/findCourse")
    public Courses findCourse(@RequestParam Integer courseId);

    @GetMapping("/joinCourseByStuId")
    public JoinClass joinCourseByStuId(@RequestParam Integer courseId, @RequestParam Integer stuId);
    @GetMapping("/getInfo/{classId}")
    public Result<Courses> getCourseByClassId(@PathVariable Integer classId);
}
