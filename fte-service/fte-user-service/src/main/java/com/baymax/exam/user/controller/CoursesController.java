package com.baymax.exam.user.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baymax.exam.common.core.exception.ResultException;
import com.baymax.exam.common.core.result.PageResult;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.common.core.result.ResultCode;
import com.baymax.exam.file.feign.FileDetailClient;
import com.baymax.exam.user.mapper.CoursesMapper;
import com.baymax.exam.user.model.Courses;
import com.baymax.exam.user.model.JoinClass;
import com.baymax.exam.user.model.User;
import com.baymax.exam.user.service.impl.CoursesServiceImpl;
import com.baymax.exam.user.service.impl.CoursesCoverGeneratorService;
import com.baymax.exam.user.service.impl.JoinClassServiceImpl;
import com.baymax.exam.user.vo.CourseInfoVo;
import com.baymax.exam.web.annotation.Inner;
import com.baymax.exam.web.utils.UserAuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <p>
 * 课程信息 前端控制器
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
@Slf4j
@Validated
@Tag(name = "课程管理")
@RestController
@RequestMapping("/courses")
public class CoursesController {
    @Autowired
    CoursesServiceImpl coursesService;
    @Autowired
    JoinClassServiceImpl joinClassService;
    @Autowired
    CoursesMapper coursesMapper;
    @Autowired
    FileDetailClient fileDetailClient;
    @Autowired
    CoursesCoverGeneratorService coursesCoverGeneratorService;

    @Operation(summary = "创建/更新课程")
    @PostMapping("/update")
    public Result add(@RequestBody @Validated Courses courses) {
        //更新判断
        Integer courseId = courses.getId();
        Integer userId = UserAuthUtil.getUserId();
        String info="创建成功";
        //更新判断：是不是自己的课程
        if(courseId!=null){
            Courses cour = coursesService.getById(courseId);
            if(cour==null||cour.getUserId()!=userId){
                return Result.failed(ResultCode.PARAM_ERROR);
            }
            info="更新成功";
        }
        courses.setUserId(userId);
        coursesService.saveOrUpdate(courses);
        return Result.msgSuccess(info);
    }
    @Operation(summary = "更新封面")
    @PostMapping("/upload-cover")
    Result<String> uploadCoursePicture(@RequestPart("file") MultipartFile file) throws ResultException {
        Integer userId = UserAuthUtil.getUserId();
        Result<String> result = fileDetailClient.uploadImage(file, "/"+userId+"/course/", null, "course");
        String url = result.getResultDate();
        return Result.success("上传成功",url);
    }

    @Operation(summary = "生成课程封面")
    @PostMapping("/generate-cover")
    Result<String> generateCourseCover(@RequestBody Map<String, String> params) throws ResultException {
        String courseName = params.get("courseName");
        if(courseName == null || courseName.trim().isEmpty()) {
            return Result.failed(ResultCode.PARAM_ERROR, "课程名称不能为空");
        }

        // 调用封面生成服务
        String coverUrl = coursesCoverGeneratorService.generateCourseCover(courseName);
        return Result.success("生成成功", coverUrl);
    }

    @Operation(summary = "获取课程课程")
    @GetMapping("/getInfo")
    public Result<Courses> getCourse(@RequestParam Integer courseId) {
        //
        final long startTime = System.currentTimeMillis();
        Courses course = coursesService.getById(courseId);
        Integer userId = UserAuthUtil.getUserId();
        // 未公开:未登录/未加入 禁止获取课程信息
        if("0".equals(course.getIsPublic())&&!userId.equals(course.getUserId())){
            if(userId==null){
                return Result.failed(ResultCode.PARAM_ERROR);
            }
            JoinClass joinInfo = joinClassService.getJoinByCourseId(userId, courseId);
            if(joinInfo==null){
                return Result.msgWaring("未加入该课程~");
            }
        }
        final long endtTime = System.currentTimeMillis();
        log.info("课程耗时："+(endtTime-startTime));
        return Result.success(coursesService.getCourseInfo(courseId));
    }
    @Operation(summary = "获取班级课程")
    @GetMapping("/getInfo/{classId}")
    public Result<Courses> getCourseByClassId(@PathVariable Integer classId){
        Integer userId = UserAuthUtil.getUserId();
        Courses course = coursesMapper.getCourseByClassId(classId);
        if(course.getUserId()==userId){
            return Result.success(course);
        }
        JoinClass joinClass = joinClassService.getJoinByClassId(userId,classId);
        if(joinClass==null){
             return Result.failed(ResultCode.PARAM_ERROR);
        }
        return Result.success(course);
    }
    @Operation(summary = "分页获取课程列表")
    @GetMapping("/list/{role}")
    public Result<PageResult<CourseInfoVo>> getCourseList(
            @PathVariable @Schema(description = "student/teacher") String role,
            @Schema(description = "0:开课：1：结课") @RequestParam  String status,
            @RequestParam Long currentPage) {
        boolean isStudent = true;
        int stu=0;
        if ("teacher".equals(role)) {
            isStudent = false;
        }
        if("1".equals(status)){
            stu=1;
        }
        IPage<CourseInfoVo> courseList = coursesService.getCourseList(UserAuthUtil.getUserId(),stu, isStudent, currentPage, 12);
        return Result.success(PageResult.setResult(courseList));
    }

    @Operation(summary = "修改公开/结课状态")
    @PostMapping("/update/{status}")
    public Result updatePublicOrStatus(
            @Schema(description = "status:：status/public") @PathVariable String status,
            @Schema(description = "课程id")@RequestParam Integer courseId,
            @Schema(description = "状态:0：开课/隐藏，1：结课/公共") @RequestParam String isPublic) {
        Courses myCourse = coursesService.getById(courseId);
        if(myCourse==null||myCourse.getUserId()!=UserAuthUtil.getUserId()){
            return Result.failed(ResultCode.PARAM_ERROR);
        }
        Courses courses = new Courses();
        courses.setUserId(UserAuthUtil.getUserId());
        courses.setId(courseId);
        if ("public".equals(status)) {
            courses.setStatus(isPublic);
        } else {
            courses.setIsPublic(isPublic);
        }
        coursesService.updateById(courses);
        return Result.msgSuccess("修改成功");
    }
    @Inner
    @Operation(summary = "id获取课程信息")
    @GetMapping("/findCourse")
    public Courses findCourse(Integer courseId){
        return coursesService.getById(courseId);
    }
    @Inner
    @Operation(summary = "获取课程参加信息")
    @GetMapping("/joinCourseByStuId")
    public JoinClass joinCourseByStuId(Integer courseId, Integer stuId){
        return joinClassService.getJoinByCourseId(stuId,courseId);
    }
}
