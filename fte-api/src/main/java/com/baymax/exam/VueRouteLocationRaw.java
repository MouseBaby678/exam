package com.baymax.exam;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：vue 路由参数
 * @modified By：
 * @version:
 */
@Data
public class VueRouteLocationRaw {
    String name;
    String path;
    Map<String,Object> params;
    Map<String,Object> query;
    @JsonIgnore
    public String getJson(){
        return JSONUtil.toJsonStr(this);
    }
}
