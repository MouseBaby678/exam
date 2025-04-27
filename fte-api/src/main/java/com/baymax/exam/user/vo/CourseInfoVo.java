package com.baymax.exam.user.vo;

import com.baymax.exam.user.model.Courses;
import com.baymax.exam.user.model.User;
import lombok.Data;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：课程信息
 * @modified By：
 * @version:
 */
@Data
public class CourseInfoVo extends Courses{
    private User teacher;
}
