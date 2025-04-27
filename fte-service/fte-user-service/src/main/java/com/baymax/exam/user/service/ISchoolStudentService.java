package com.baymax.exam.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.user.model.SchoolStudent;

import java.util.List;

/**
 * <p>
 * 学校用户认证信息 服务类
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
public interface ISchoolStudentService extends IService<SchoolStudent> {

    /**
     * 根据学号和姓名查询学生信息
     *
     * @param jobNo 学号
     * @param realName 姓名
     * @return 学生信息
     */
    SchoolStudent getStudentByJobNoAndRealName(String jobNo, String realName);

    /**
     * 批量导入学生信息
     *
     * @param students 学生信息列表
     * @return 结果
     */
    Result<Object> batchImportStudents(List<SchoolStudent> students);

    /**
     * 检查学号是否已存在
     *
     * @param jobNo 学号
     * @return 是否存在
     */
    boolean checkJobNoExists(String jobNo);

    /**
     * 获取学院下的学生列表
     *
     * @param departmentId 学院ID
     * @return 学生列表
     */
    List<SchoolStudent> getStudentsByDepartmentId(Integer departmentId);

    /**
     * 更新学生状态
     *
     * @param id 学生ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStudentStatus(Integer id, Integer status);
}
