package com.baymax.exam.center.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：
 * @modified By：
 * @version:
 */
public enum QuestionResultTypeEnum {
    NONE(10,"未批阅"),
    ERROR(20,"错误"),
    WRONG(30,"半错"),
    CORRECT(40,"正确");

    @Getter
    @EnumValue
    int value;
    @Getter
    String label;
    QuestionResultTypeEnum(int value,String label){
        this.value=value;
        this.label=label;
    }
}
