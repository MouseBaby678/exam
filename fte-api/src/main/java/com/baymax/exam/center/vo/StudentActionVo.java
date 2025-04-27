package com.baymax.exam.center.vo;

import com.baymax.exam.center.enums.AnswerStatusEnum;
import com.baymax.exam.center.model.ExamAnswerLog;
import com.baymax.exam.common.core.result.PageResult;
import com.baymax.exam.user.model.UserAuthInfo;
import lombok.Data;

/**
 * @author ：MouseBaby678
 * @date ：Created in 2025/4/27 16:51
 * @description：学生行为
 * @modified By：
 * @version:
 */
@Data
public class StudentActionVo {
    private UserAuthInfo userAuthInfo;
    private PageResult<ExamAnswerLog> actionPage;
    private AnswerStatusEnum answerStatus;
}
