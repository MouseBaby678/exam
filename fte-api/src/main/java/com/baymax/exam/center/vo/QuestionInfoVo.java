package com.baymax.exam.center.vo;

import com.baymax.exam.center.model.Question;
import com.baymax.exam.center.model.QuestionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：题目信息
 * @modified By：
 * @version:
 */
@Data
@Schema(name = "QuestionInfoVo", description = "题目具体信息")
public class QuestionInfoVo extends Question {
    private List<QuestionItem> options;
}
