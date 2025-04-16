package com.baymax.exam.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.user.mapper.SchoolStudentMapper;
import com.baymax.exam.user.model.SchoolStudent;
import com.baymax.exam.user.service.ISchoolStudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学校用户认证信息 服务实现类
 * </p>
 *
 * @author baymax
 * @since 2022-12-14
 */
@Service
public class SchoolStudentServiceImpl extends ServiceImpl<SchoolStudentMapper, SchoolStudent> implements ISchoolStudentService {
    
    /**
     * 根据学号和姓名查询学生信息
     *
     * @param jobNo 学号
     * @param realName 姓名
     * @return 学生信息
     */
    @Override
    public SchoolStudent getStudentByJobNoAndRealName(String jobNo, String realName) {
        QueryWrapper<SchoolStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("job_no", jobNo).eq("real_name", realName);
        return getOne(queryWrapper);
    }
    
    /**
     * 批量导入学生信息
     *
     * @param students 学生信息列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> batchImportStudents(List<SchoolStudent> students) {
        if (students == null || students.isEmpty()) {
            return Result.msgInfo("请提供有效的学生信息列表");
        }
        
        Map<String, Object> result = new HashMap<>();
        List<SchoolStudent> successStudents = new ArrayList<>();
        List<Map<String, Object>> failedStudents = new ArrayList<>();
        
        for (SchoolStudent student : students) {
            // 验证必要字段
            if (student.getJobNo() == null || student.getRealName() == null || student.getDepartmentId() == null) {
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("student", student);
                failInfo.put("reason", "学号、姓名或部门ID为空");
                failedStudents.add(failInfo);
                continue;
            }
            
            // 检查是否已存在相同学号
            if (checkJobNoExists(student.getJobNo())) {
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("student", student);
                failInfo.put("reason", "学号已存在");
                failedStudents.add(failInfo);
                continue;
            }
            
            // 设置创建和更新时间
            student.setCreatedAt(LocalDateTime.now());
            student.setUpdatedAt(LocalDateTime.now());
            student.setStatus(1); // 默认有效
            
            // 保存学生信息
            try {
                boolean saved = save(student);
                if (saved) {
                    successStudents.add(student);
                } else {
                    Map<String, Object> failInfo = new HashMap<>();
                    failInfo.put("student", student);
                    failInfo.put("reason", "保存失败");
                    failedStudents.add(failInfo);
                }
            } catch (Exception e) {
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("student", student);
                failInfo.put("reason", "保存异常: " + e.getMessage());
                failedStudents.add(failInfo);
            }
        }
        
        result.put("success", successStudents);
        result.put("failed", failedStudents);
        result.put("totalCount", students.size());
        result.put("successCount", successStudents.size());
        result.put("failCount", failedStudents.size());
        
        Result<Object> response = Result.success(result);
        response.setMsg("批量导入完成，成功" + successStudents.size() + "条，失败" + failedStudents.size() + "条");
        return response;
    }
    
    /**
     * 检查学号是否已存在
     *
     * @param jobNo 学号
     * @return 是否存在
     */
    @Override
    public boolean checkJobNoExists(String jobNo) {
        QueryWrapper<SchoolStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("job_no", jobNo);
        return count(queryWrapper) > 0;
    }
    
    /**
     * 获取学院下的学生列表
     *
     * @param departmentId 学院ID
     * @return 学生列表
     */
    @Override
    public List<SchoolStudent> getStudentsByDepartmentId(Integer departmentId) {
        QueryWrapper<SchoolStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_id", departmentId);
        return list(queryWrapper);
    }
    
    /**
     * 更新学生状态
     *
     * @param id 学生ID
     * @param status 状态
     * @return 是否成功
     */
    @Override
    public boolean updateStudentStatus(Integer id, Integer status) {
        LambdaUpdateWrapper<SchoolStudent> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SchoolStudent::getId, id);
        updateWrapper.set(SchoolStudent::getStatus, status);
        updateWrapper.set(SchoolStudent::getUpdatedAt, LocalDateTime.now());
        return update(updateWrapper);
    }
}
