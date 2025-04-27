package com.baymax.exam.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.user.mapper.SchoolAuthMapper;
import com.baymax.exam.user.model.SchoolAuth;
import com.baymax.exam.user.model.User;
import com.baymax.exam.user.model.UserAuthInfo;
import com.baymax.exam.user.service.ISchoolAuthService;
import com.baymax.exam.user.service.IUserAuthInfoService;
import com.baymax.exam.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学生认证表 服务实现类
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
@Service
@Slf4j
public class SchoolAuthServiceImpl extends ServiceImpl<SchoolAuthMapper, SchoolAuth> implements ISchoolAuthService {

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserAuthInfoService userAuthInfoService;

    /**
     * 绑定学生认证
     *
     * @param userId 用户ID
     * @param studentId 学生ID
     * @return 认证结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserAuthInfo> bindStudentAuth(Integer userId, Integer studentId) {
        try {
            log.info("开始绑定学生认证，用户ID：{}，学生ID：{}", userId, studentId);

            // 参数校验
            if (userId == null || studentId == null) {
                return Result.msgInfo("参数不能为空");
            }

            // 检查是否已认证
            if (checkUserAuthExists(userId)) {
                log.warn("用户已完成认证，不能重复认证，用户ID：{}", userId);
                return Result.msgInfo("该用户已完成认证，不能重复认证");
            }

            // 检查学生信息是否已被绑定
            if (checkStudentAuthExists(studentId)) {
                log.warn("学生信息已被其他账号认证，学生ID：{}", studentId);
                return Result.msgInfo("该学生信息已被其他账号认证，如有疑问请联系管理员");
            }

            // 创建认证记录
            SchoolAuth auth = new SchoolAuth();
            auth.setUserId(userId);
            auth.setStudentId(studentId);
            auth.setStatus(1); // 默认有效
            auth.setCreatedAt(LocalDateTime.now());
            auth.setUpdatedAt(LocalDateTime.now());

            boolean saved = save(auth);
            if (!saved) {
                log.error("保存认证记录失败，userId: {}, studentId: {}", userId, studentId);
                return Result.msgInfo("认证失败，保存认证记录失败");
            }

            log.info("认证记录保存成功，ID: {}", auth.getId());

            // 更新用户认证ID
            User user = userService.getById(userId);
            if (user == null) {
                log.error("获取用户信息失败，userId: {}", userId);
                return Result.msgInfo("认证失败，用户信息获取失败");
            }

            user.setAuthId(auth.getId());
            boolean userUpdated = userService.updateById(user);
            if (!userUpdated) {
                log.error("更新用户认证ID失败，userId: {}", userId);
                return Result.msgInfo("认证失败，更新用户信息失败");
            }

            // 返回认证后的用户信息
            QueryWrapper<UserAuthInfo> infoQueryWrapper = new QueryWrapper<>();
            infoQueryWrapper.eq("user_id", userId);
            UserAuthInfo userAuthInfo = userAuthInfoService.getOne(infoQueryWrapper);

            if (userAuthInfo == null) {
                log.warn("获取认证用户信息为空，userId: {}", userId);
                return Result.msgInfo("认证成功，但获取认证信息失败");
            }

            log.info("用户认证成功，userId: {}, authId: {}", userId, auth.getId());
            Result<UserAuthInfo> result = Result.success(userAuthInfo);
            result.setMsg("认证成功");
            return result;
        } catch (Exception e) {
            log.error("认证过程发生异常，userId: {}, studentId: {}, error: {}", userId, studentId, e.getMessage(), e);
            return Result.msgInfo("认证失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户认证信息
     *
     * @param userId 用户ID
     * @return 认证信息
     */
    @Override
    public Result<UserAuthInfo> getUserAuthInfo(Integer userId) {
        QueryWrapper<UserAuthInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        UserAuthInfo userAuthInfo = userAuthInfoService.getOne(queryWrapper);

        if (userAuthInfo == null || userAuthInfo.getStudentId() == null) {
            return Result.msgInfo("用户未完成认证");
        }

        return Result.success(userAuthInfo);
    }

    /**
     * 解除用户认证
     *
     * @param userId 用户ID
     * @return 解除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelAuth(Integer userId) {
        try {
            log.info("开始解除用户认证，用户ID：{}", userId);

            // 参数校验
            if (userId == null) {
                return Result.msgInfo("用户ID不能为空");
            }

            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                log.warn("用户不存在，userId: {}", userId);
                return Result.msgInfo("用户不存在");
            }

            if (user.getAuthId() == null) {
                log.warn("用户未完成认证，userId: {}", userId);
                return Result.msgInfo("用户未完成认证");
            }

            // 获取认证信息
            Integer authId = user.getAuthId();
            SchoolAuth auth = getById(authId);
            if (auth == null) {
                log.warn("认证信息不存在，authId: {}", authId);
                // 即使认证记录不存在，也清空用户的authId
                user.setAuthId(null);
                userService.updateById(user);
                return Result.msgInfo("认证信息不存在，已清除用户认证状态");
            }

            // 先解除用户认证ID
            user.setAuthId(null);
            boolean userUpdated = userService.updateById(user);
            if (!userUpdated) {
                log.error("更新用户认证ID失败，userId: {}", userId);
                return Result.msgInfo("解除认证失败，更新用户信息失败");
            }

            // 然后删除认证记录
            boolean removed = removeById(auth.getId());
            if (!removed) {
                log.error("删除认证记录失败，authId: {}", authId);
                // 如果删除失败但用户已更新，不回滚，保留用户的更新
                return Result.msgInfo("认证记录删除失败，但用户认证状态已解除");
            }

            log.info("用户认证解除成功，userId: {}, authId: {}", userId, authId);
            return Result.success("解除认证成功");
        } catch (Exception e) {
            log.error("解除认证过程发生异常，userId: {}, error: {}", userId, e.getMessage(), e);
            return Result.msgInfo("解除认证失败：" + e.getMessage());
        }
    }

    /**
     * 检查学生是否已被认证
     *
     * @param studentId 学生ID
     * @return 是否已认证
     */
    @Override
    public boolean checkStudentAuthExists(Integer studentId) {
        QueryWrapper<SchoolAuth> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        return count(queryWrapper) > 0;
    }

    /**
     * 检查用户是否已认证
     *
     * @param userId 用户ID
     * @return 是否已认证
     */
    @Override
    public boolean checkUserAuthExists(Integer userId) {
        QueryWrapper<SchoolAuth> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return count(queryWrapper) > 0;
    }

    /**
     * 批量解除认证
     *
     * @param userIds 用户ID列表
     * @return 批量解除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> batchCancelAuth(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.msgInfo("请提供有效的用户ID列表");
        }

        Map<String, Object> result = new HashMap<>();
        List<Integer> successIds = new ArrayList<>();
        List<Map<String, Object>> failedIds = new ArrayList<>();

        for (Integer userId : userIds) {
            try {
                // 获取用户信息
                User user = userService.getById(userId);
                if (user == null || user.getAuthId() == null) {
                    Map<String, Object> failInfo = new HashMap<>();
                    failInfo.put("userId", userId);
                    failInfo.put("reason", "用户不存在或未完成认证");
                    failedIds.add(failInfo);
                    continue;
                }

                // 获取认证信息
                SchoolAuth auth = getById(user.getAuthId());
                if (auth == null) {
                    Map<String, Object> failInfo = new HashMap<>();
                    failInfo.put("userId", userId);
                    failInfo.put("reason", "认证信息不存在");
                    failedIds.add(failInfo);
                    continue;
                }

                // 解除用户认证ID
                user.setAuthId(null);
                boolean userUpdated = userService.updateById(user);

                // 删除认证记录
                boolean authRemoved = removeById(auth.getId());

                if (userUpdated && authRemoved) {
                    successIds.add(userId);
                } else {
                    Map<String, Object> failInfo = new HashMap<>();
                    failInfo.put("userId", userId);
                    failInfo.put("reason", "解除认证失败");
                    failedIds.add(failInfo);
                }
            } catch (Exception e) {
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("userId", userId);
                failInfo.put("reason", "操作异常: " + e.getMessage());
                failedIds.add(failInfo);
            }
        }

        result.put("success", successIds);
        result.put("failed", failedIds);
        result.put("totalCount", userIds.size());
        result.put("successCount", successIds.size());
        result.put("failCount", failedIds.size());

        Result<Object> response = Result.success(result);
        response.setMsg("批量解除认证完成，成功" + successIds.size() + "条，失败" + failedIds.size() + "条");
        return response;
    }

    /**
     * 直接通过ID删除认证记录
     *
     * @param authId 认证记录ID
     * @return 删除的记录数
     */
    @Override
    public int deleteAuthById(Integer authId) {
        return baseMapper.deleteAuthById(authId);
    }
}
