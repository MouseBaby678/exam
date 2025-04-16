package com.baymax.exam.user.mapper;

import com.baymax.exam.user.model.SchoolAuth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 学生认证表 Mapper 接口
 * </p>
 *
 * @author baymax
 * @since 2022-12-14
 */
@Mapper
public interface SchoolAuthMapper extends BaseMapper<SchoolAuth> {
    
    /**
     * 通过ID直接删除认证记录
     * 
     * @param authId 认证记录ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM es_school_auth WHERE id = #{authId}")
    int deleteAuthById(@Param("authId") Integer authId);
}
