package com.baymax.exam.user.mapper;

import com.baymax.exam.user.model.Classes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
public interface ClassesMapper extends BaseMapper<Classes> {
    Classes getClassByUserId(int courseId,int userId);
}
