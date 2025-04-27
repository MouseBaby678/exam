package com.baymax.exam.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.user.model.User;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
public interface IUserService extends IService<User> {
    /**
     * 添加用户
     *
     * @param user 用户
     * @return {@link Boolean}
     */
    public Result<String> addUser(User user);

    /**
     * 更新用户
     *
     * @param user 用户
     * @return {@link Result}<{@link String}>
     */
    public Result<String> updateUser(User user);

    /**
     * 用户名获取用户
     *
     * @param username 用户名
     * @return {@link User}
     */
    public User getUserByUserName(String username);

    /**
     * 通过电子邮件获取用户
     *
     * @param email 电子邮件
     * @return {@link User}
     */
    public User getUserByEmail(String email);
}
