package com.baymax.exam.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.user.model.SchoolAuth;
import com.baymax.exam.user.model.SchoolStudent;
import com.baymax.exam.user.model.User;
import com.baymax.exam.user.model.UserAuthInfo;
import com.baymax.exam.user.service.ISchoolAuthService;
import com.baymax.exam.user.service.ISchoolStudentService;
import com.baymax.exam.user.service.IUserAuthInfoService;
import com.baymax.exam.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学生认证表 前端控制器
 * </p>
 *
 * @author baymax
 * @since 2022-12-14
 */
@RestController
@RequestMapping("/school-auth")
@Tag(name = "学校认证接口")
@Slf4j
public class SchoolAuthController {

    @Autowired
    private ISchoolAuthService schoolAuthService;
    
    @Autowired
    private ISchoolStudentService schoolStudentService;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private IUserAuthInfoService userAuthInfoService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/verify")
    @Operation(summary = "校验学生信息", description = "根据学号和姓名校验学生信息")
    @Parameters({
        @Parameter(name = "jobNo", description = "学生学号", in = ParameterIn.QUERY),
        @Parameter(name = "realName", description = "学生姓名", in = ParameterIn.QUERY)
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "学生信息验证成功", content = @Content(schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "未找到匹配的学生信息，请确认学号和姓名是否正确")
    })
    public Result<Map<String, Object>> verifyStudent(@RequestParam String jobNo, @RequestParam String realName) {
        SchoolStudent student = schoolStudentService.getStudentByJobNoAndRealName(jobNo, realName);
        
        if (student == null) {
            return Result.msgInfo("未找到匹配的学生信息，请确认学号和姓名是否正确");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("studentId", student.getId());
        data.put("departmentId", student.getDepartmentId());
        data.put("jobNo", student.getJobNo());
        data.put("realName", student.getRealName());
        
        Result<Map<String, Object>> result = Result.success(data);
        result.setMsg("学生信息验证成功");
        return result;
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定学生认证", description = "将用户与学生信息进行绑定认证")
    @Parameters({
        @Parameter(name = "userId", description = "用户ID", in = ParameterIn.QUERY),
        @Parameter(name = "studentId", description = "学生ID", in = ParameterIn.QUERY)
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "认证成功", content = @Content(schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "该用户已完成认证，不能重复认证"),
        @ApiResponse(responseCode = "400", description = "该学生信息已被其他账号认证，如有疑问请联系管理员")
    })
    public Result<UserAuthInfo> bindStudentAuth(@RequestParam Integer userId, @RequestParam Integer studentId) {
        return schoolAuthService.bindStudentAuth(userId, studentId);
    }

    @GetMapping("/info/{userId}")
    @Operation(summary = "获取用户认证信息", description = "根据用户ID获取认证信息")
    @Parameter(name = "userId", description = "用户ID", in = ParameterIn.PATH)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "用户未完成认证")
    })
    public Result<UserAuthInfo> getUserAuthInfo(@PathVariable Integer userId) {
        return schoolAuthService.getUserAuthInfo(userId);
    }
    
    @DeleteMapping("/{userId}")
    @Operation(summary = "解除用户认证", description = "解除用户与学生信息的绑定")
    @Parameter(name = "userId", description = "用户ID", in = ParameterIn.PATH)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "解除认证成功", content = @Content(schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "用户未完成认证或不存在")
    })
    public Result<String> cancelAuth(@PathVariable Integer userId) {
        return schoolAuthService.cancelAuth(userId);
    }
    
    @PostMapping("/import-students")
    @Operation(summary = "批量导入学生", description = "批量导入学生信息到认证库")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "批量导入完成", content = @Content(schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "请提供有效的学生信息列表")
    })
    public Result<Object> importStudents(@RequestBody List<SchoolStudent> students) {
        return schoolStudentService.batchImportStudents(students);
    }
    
    @GetMapping("/status")
    @Operation(summary = "获取认证状态", description = "获取当前登录用户的认证状态")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取认证状态成功", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    public Result<Map<String, Object>> getAuthStatus() {
        try {
            // 尝试从请求上下文获取当前用户ID
            Integer userId = null;
            // TODO: 从安全上下文中获取实际用户ID，这里需要根据项目的认证机制进行调整
            
            // 如果无法获取用户ID，从请求参数中获取
            if (userId == null) {
                // 这里可以通过其他方式获取，如请求参数
                userId = 1; // 测试用，实际应从安全上下文获取
                log.warn("无法从安全上下文获取用户ID，使用默认值：{}", userId);
            }
            
            log.info("获取用户认证状态，userId: {}", userId);
            
            // 直接查询数据库获取最新状态，避免缓存问题
            User user = userService.getById(userId);
            boolean isAuthenticated = false;
            
            if (user != null && user.getAuthId() != null) {
                // 通过用户的authId字段直接判断
                isAuthenticated = true;
                log.info("用户已认证，userId: {}, authId: {}", userId, user.getAuthId());
            } else {
                // 双重检查：如果user.authId为空，再通过user_id查询认证表确认
                isAuthenticated = schoolAuthService.checkUserAuthExists(userId);
                log.info("用户认证状态(通过认证表查询)：{}, userId: {}", isAuthenticated, userId);
                
                // 如果存在不一致，尝试修复用户表中的authId
                if (isAuthenticated && (user == null || user.getAuthId() == null)) {
                    log.warn("发现用户认证状态不一致，需要修复，userId: {}", userId);
                    try {
                        // 获取认证记录
                        QueryWrapper<SchoolAuth> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("user_id", userId);
                        SchoolAuth auth = schoolAuthService.getOne(queryWrapper);
                        
                        if (auth != null && user != null) {
                            // 更新用户的authId
                            user.setAuthId(auth.getId());
                            userService.updateById(user);
                            log.info("用户认证状态已修复，userId: {}, authId: {}", userId, auth.getId());
                        }
                    } catch (Exception e) {
                        log.error("修复用户认证状态失败", e);
                    }
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", isAuthenticated ? 1 : 0);
            result.put("userId", userId);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取认证状态异常", e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", 0);  // 异常情况下默认未认证
            return Result.success(result);
        }
    }

    /**
     * 完整解除认证流程（解决外键约束问题）
     * @param userId 用户ID
     * @return 解除结果
     */
    @PostMapping("/full-cancel/{userId}")
    @Operation(summary = "完整解除认证流程", description = "解决外键约束问题的完整解除认证流程")
    @Parameter(name = "userId", description = "用户ID", in = ParameterIn.PATH)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "解除成功"),
        @ApiResponse(responseCode = "400", description = "用户未完成认证或不存在")
    })
    public Result<String> fullCancelAuth(@PathVariable Integer userId) {
        try {
            log.info("开始执行完整解除认证流程，用户ID：{}", userId);
            
            // 参数校验
            if (userId == null) {
                return Result.msgInfo("用户ID不能为空");
            }
            
            // 1. 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                log.warn("用户不存在，userId: {}", userId);
                return Result.msgInfo("用户不存在");
            }
            
            if (user.getAuthId() == null) {
                log.warn("用户未完成认证，userId: {}", userId);
                return Result.msgInfo("用户未完成认证");
            }
            
            // 2. 获取认证ID
            Integer authId = user.getAuthId();
            log.info("获取到用户认证ID：{}", authId);
            
            // 3. 使用JdbcTemplate直接执行SQL更新用户表，避开MyBatis和外键约束
            int updateResult = jdbcTemplate.update(
                "UPDATE es_user SET auth_id = NULL WHERE id = ?", 
                userId
            );
            
            if (updateResult <= 0) {
                log.error("解除认证失败：无法更新用户信息，userId: {}", userId);
                return Result.msgInfo("解除认证失败：无法更新用户信息");
            }
            
            log.info("用户认证ID清除成功，userId: {}", userId);
            
            // 4. 使用JdbcTemplate直接执行SQL删除认证记录
            int deleteResult = jdbcTemplate.update(
                "DELETE FROM es_school_auth WHERE id = ?", 
                authId
            );
            
            if (deleteResult > 0) {
                log.info("认证记录删除成功，authId: {}", authId);
                return Result.success("解除认证成功");
            } else {
                // 如果删除失败但用户已更新，不回滚，保留用户的更新
                log.warn("认证记录删除失败，但用户认证状态已解除，authId: {}", authId);
                return Result.msgInfo("认证记录删除失败，但用户认证状态已解除");
            }
        } catch (Exception e) {
            log.error("解除认证过程发生异常，userId: {}, error: {}", userId, e.getMessage(), e);
            return Result.msgInfo("解除认证出现异常: " + e.getMessage());
        }
    }
} 