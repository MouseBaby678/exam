package com.baymax.exam.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baymax.exam.user.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户信息 Mapper 接口
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
