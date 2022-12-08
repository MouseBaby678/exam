package com.baymax.exam.center.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baymax.exam.center.enums.QuestionResultTypeEnum;
import com.baymax.exam.center.enums.ReviewTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 考试作答结果
 * </p>
 *
 * @author baymax
 * @since 2022-12-06
 */
@Getter
@Setter
@TableName("ed_exam_answer_result")
@Schema(name = "ExamAnswerResult", description = "考试作答结果")
public class ExamAnswerResult implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    @Schema(description = "考试id")
    private Integer examInfoId;

    @Schema(description = "题目id")
    private Integer questionId;

    @Schema(description = "选项id")
    private Integer optionId;

    @Schema(description = "答案：主观题使用")
    private String answer;

    @Schema(description = "得分")
    private Float score;

    @Schema(description = "结果类型：对、错、半错")
    private QuestionResultTypeEnum resultType;

    @Schema(description = "批阅类型：机器、老师")
    private ReviewTypeEnum reviewType;

    @Schema(description = "评价")
    private String evaluate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
