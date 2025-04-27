package com.baymax.exam.center.vo;

import com.baymax.exam.center.model.ExamPaper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：
 * @modified By：
 * @version:
 */
@Data
@Schema(name = "ExamPaperVo", description = "试卷题目具体信息")
public class ExamPaperVo {
    @Valid
    @NotNull(message = "试卷信息不能为空")
    ExamPaper examPaper;
    Set<Integer> questions;
}
