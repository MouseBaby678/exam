package com.baymax.exam.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.user.model.SchoolAuth;
import com.baymax.exam.user.model.UserAuthInfo;

/**
 * <p>
 * 学生认证表 服务类
 * </p>
 *
 * @author baymax
 * @since 2022-12-14
 */
public interface ISchoolAuthService extends IService<SchoolAuth> {
    
    /**
     * 绑定学生认证
     *
     * @param userId 用户ID
     * @param studentId 学生ID
     * @return 认证结果
     */
    Result<UserAuthInfo> bindStudentAuth(Integer userId, Integer studentId);
    
    /**
     * 获取用户认证信息
     *
     * @param userId 用户ID
     * @return 认证信息
     */
    Result<UserAuthInfo> getUserAuthInfo(Integer userId);
    
    /**
     * 解除用户认证
     *
     * @param userId 用户ID
     * @return 解除结果
     */
    Result<String> cancelAuth(Integer userId);
    
    /**
     * 检查学生是否已被认证
     *
     * @param studentId 学生ID
     * @return 是否已认证
     */
    boolean checkStudentAuthExists(Integer studentId);
    
    /**
     * 检查用户是否已认证
     *
     * @param userId 用户ID
     * @return 是否已认证
     */
    boolean checkUserAuthExists(Integer userId);
    
    /**
     * 批量解除认证
     *
     * @param userIds 用户ID列表
     * @return 批量解除结果
     */
    Result<Object> batchCancelAuth(java.util.List<Integer> userIds);
    
    /**
     * 直接通过ID删除认证记录（用于解决外键约束问题）
     *
     * @param authId 认证记录ID
     * @return 删除的记录数
     */
    int deleteAuthById(Integer authId);
}
