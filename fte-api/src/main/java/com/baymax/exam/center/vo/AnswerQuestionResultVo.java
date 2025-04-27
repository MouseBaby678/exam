package com.baymax.exam.center.vo;

import com.baymax.exam.center.model.ExamAnswerResult;
import com.baymax.exam.center.model.ExamScoreRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：
 * @modified By：
 * @version:
 */
@Data
public class AnswerQuestionResultVo {
    @Schema(description = "题目信息")
    private QuestionInfoVo questionInfo;
    @Schema(description = "作答信息")
    private List<ExamAnswerResult> answerResult;
    @Schema(description = "批阅结果")
    private ExamScoreRecord scoreRecord;
}
