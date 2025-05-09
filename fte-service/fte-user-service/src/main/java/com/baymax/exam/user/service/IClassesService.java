package com.baymax.exam.user.service;

import com.baymax.exam.user.model.Classes;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
public interface IClassesService extends IService<Classes> {

    /**
     * 根据课程id获取
     *
     * @param courseId 进程id
     * @return {@link Classes}
     */
    List<Classes> getClassByCourseId(Integer courseId);
}
