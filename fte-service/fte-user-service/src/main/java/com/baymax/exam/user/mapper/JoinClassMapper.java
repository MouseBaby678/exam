package com.baymax.exam.user.mapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baymax.exam.user.model.JoinClass;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baymax.exam.user.model.UserAuthInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author MouseBaby678
 * @since 2025-4-27
 */
@Mapper
public interface JoinClassMapper extends BaseMapper<JoinClass> {

    IPage<UserAuthInfo>  getJoinClassUser(IPage<UserAuthInfo> page, Wrapper<JoinClass> ew);
    JoinClass getJoinByCourseId(Integer courseId,Integer userId);

    Integer getStudentNumberByIds(Collection<Integer> ids,Integer userId);
}
