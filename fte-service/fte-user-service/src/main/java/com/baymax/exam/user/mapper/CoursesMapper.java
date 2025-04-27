package com.baymax.exam.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baymax.exam.user.model.Courses;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baymax.exam.user.vo.CourseInfoVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 课程信息 Mapper 接口
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
@Mapper
public interface CoursesMapper extends BaseMapper<Courses> {
    IPage<CourseInfoVo> getCourseList(IPage<CourseInfoVo> page, QueryWrapper<CourseInfoVo> ew, Boolean isStudent);
    CourseInfoVo getCourseInfo(Integer courseId);
    Courses getCourseByClassId(Integer classId);
}
