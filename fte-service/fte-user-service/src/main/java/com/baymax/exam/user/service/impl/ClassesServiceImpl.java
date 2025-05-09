package com.baymax.exam.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baymax.exam.common.redis.utils.RedisUtils;
import com.baymax.exam.user.model.Classes;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baymax.exam.user.mapper.ClassesMapper;
import com.baymax.exam.user.service.IClassesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
@Service
public class ClassesServiceImpl extends ServiceImpl<ClassesMapper, Classes> implements IClassesService {

    @Autowired
    RedisUtils redisUtils;

    /**
     * 根据课程id获取
     *
     * @param courseId 进程id
     * @return {@link Classes}
     */
    @Override
    public List<Classes> getClassByCourseId(Integer courseId) {
        LambdaQueryWrapper<Classes> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(Classes::getCourseId,courseId);
        return list(queryWrapper);
    }
    public List<Classes> getClassByIds(Integer courseId, Collection<Integer> ids) {
        LambdaQueryWrapper<Classes> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(Classes::getCourseId,courseId);
        queryWrapper.in(Classes::getId,ids);
        return list(queryWrapper);
    }

}
