package com.baymax.exam.center.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baymax.exam.center.enums.DefaultQuestionRuleEnum;
import com.baymax.exam.center.enums.QuestionTypeEnum;
import com.baymax.exam.center.enums.QuestionVisibleEnum;
import com.baymax.exam.center.model.Question;
import com.baymax.exam.center.service.impl.QuestionServiceImpl;
import com.baymax.exam.center.utils.ParseQuestionText;
import com.baymax.exam.center.model.ParseQuestionRules;
import com.baymax.exam.center.vo.BatchQuestion;
import com.baymax.exam.center.vo.ParseQuestionVo;
import com.baymax.exam.center.vo.QuestionInfoVo;
import com.baymax.exam.common.core.result.PageResult;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.common.core.result.ResultCode;
import com.baymax.exam.user.feign.CourseClient;
import com.baymax.exam.user.feign.UserClient;
import com.baymax.exam.user.model.Courses;
import com.baymax.exam.user.model.JoinClass;
import com.baymax.exam.web.utils.UserAuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 题目信息 前端控制器
 * </p>
 *规定：所有类型题目至少要有一下选项，如主观题，就算没有答案，都要给一个选项
 * @author baymax
 * @since 2022-10-18
 */
@Slf4j
@Validated
@Tag(name = "题目管理")
@RestController
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    QuestionServiceImpl questionService;
    @Autowired
    CourseClient courseClient;
    @Operation(summary = "创建题目")
    @PostMapping("/add")
    public Result add(@RequestBody @Validated QuestionInfoVo questionInfo){
        //判断课程是不是自己的
        Courses course = courseClient.findCourse(questionInfo.getCourseId());
        Integer userId = UserAuthUtil.getUserId();
        if(course==null||course.getUserId()!=userId){
            return Result.failed(ResultCode.PARAM_ERROR);
        }
        questionInfo.setTeacherId(userId);
        questionInfo.setId(null);
        String result = questionService.addQuestion(questionInfo);
        if("".equals(result)){
            return Result.msgSuccess("创建题目成功");
        }else{
            return Result.msgError(result);
        }
    }
    @Operation(summary = "批量创建题目")
    @PostMapping("/batchAdd")
    public Result batchAdd(@RequestBody @Validated BatchQuestion batchQuestion){
        Courses course = courseClient.findCourse(batchQuestion.getCourseId());
        Integer userId = UserAuthUtil.getUserId();
        if(course==null||course.getUserId()!=userId){
            return Result.failed(ResultCode.PARAM_ERROR);
        }
        List<QuestionInfoVo> list=batchQuestion.getQuestionInfos();
        Set<Question> configSet = batchQuestion.getQuestionConfig();
        List<String> result=new ArrayList<>();
        
        list.stream().forEach(i->{
            i.setTeacherId(userId);
            i.setTagId(batchQuestion.getTagId());
            i.setCourseId(batchQuestion.getCourseId());
            
            // 应用题目配置
            if(configSet != null && !configSet.isEmpty()) {
                applyQuestionConfig(i, configSet);
            }
            
            result.add(questionService.addQuestion(i));
        });
        return Result.success(result);
    }
    
    /**
     * 将题目配置应用到题目上
     * @param questionInfo 题目信息
     * @param configSet 配置集合
     */
    private void applyQuestionConfig(QuestionInfoVo questionInfo, Set<Question> configSet) {
        // 根据题目类型查找匹配的配置
        String questionType = questionInfo.getType().name();
        
        for (Question config : configSet) {
            // 检查类型是否匹配（前端可能发送的是枚举名称字符串，而不是枚举对象）
            Object configType = config.getType();
            boolean typeMatches = false;
            
            if (configType instanceof String && questionType.equals(configType)) {
                typeMatches = true;
            } else if (configType instanceof QuestionTypeEnum && 
                      questionType.equals(((QuestionTypeEnum)configType).name())) {
                typeMatches = true;
            }
            
            if (typeMatches) {
                // 应用难度设置
                if(config.getDifficulty() != null) {
                    questionInfo.setDifficulty(config.getDifficulty());
                }
                
                // 应用分值设置
                if(config.getScore() != null) {
                    questionInfo.setScore(config.getScore());
                }
                
                // 应用可见状态设置
                Object visibleStatus = config.getIsPublic();
                if(visibleStatus != null) {
                    if (visibleStatus instanceof String) {
                        questionInfo.setIsPublic(convertVisibleStatus((String)visibleStatus));
                    } else if (visibleStatus instanceof QuestionVisibleEnum) {
                        questionInfo.setIsPublic((QuestionVisibleEnum)visibleStatus);
                    }
                }
                
                break;
            }
        }
    }
    
    /**
     * 转换可见状态
     * 前端传递的是字符串表示的枚举名称（如"self"、"course"、"overt"），
     * 需要转换为QuestionVisibleEnum枚举实例
     * 
     * @param visibleStatus 可见状态字符串
     * @return QuestionVisibleEnum 枚举实例
     */
    private QuestionVisibleEnum convertVisibleStatus(String visibleStatus) {
        try {
            return QuestionVisibleEnum.valueOf(visibleStatus);
        } catch (Exception e) {
            // 默认设置为自己可见
            return QuestionVisibleEnum.self;
        }
    }
    @Operation(summary = "获取匹配题目规则")
    @GetMapping("/rules")
    public Result rules(){
        Map<String, String> collect = Arrays.stream(DefaultQuestionRuleEnum.values()).collect(Collectors.toMap(DefaultQuestionRuleEnum::name, DefaultQuestionRuleEnum::getName));
        return Result.success(collect);
    }
    @Operation(summary = "解析题目文本")
    @PostMapping("/analyze")
    public Result analyze(@RequestBody @Validated ParseQuestionVo parseQuestionVo){
        try {
            // TODO:还是后端呢？
            ParseQuestionRules rule= DefaultQuestionRuleEnum.CHAOXING.getRule();
            if(parseQuestionVo.getCustomRule()!=null){
                rule= parseQuestionVo.getCustomRule();
            }else if(parseQuestionVo.getDefaultRule()!=null){
                rule = parseQuestionVo.getDefaultRule().getRule();
            }
            //将富文本换行改成\n
            String text= parseQuestionVo.getQuestionsText().replaceAll("<br\\/?>","\n");
            //去除富文本最外层p
            text=text.replaceAll("<p>|<\\/p>","");
            log.info("题目文本",text);
            log.info("题目文本"+text);
            
            List<QuestionInfoVo> parsedQuestions = ParseQuestionText.parse(text,rule);
            log.info("解析结果数量: {}", parsedQuestions.size());
            
            return Result.success(parsedQuestions);
        } catch (Exception e) {
            log.error("解析题目时发生异常", e);
            return Result.failed("解析题目失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新题目")
    @PostMapping("/update")
    public Result update(@RequestBody Question question){
        //判断题目是不是自己的
        Question qu = questionService.getById(question.getId());
        Integer userId = UserAuthUtil.getUserId();
        if(qu==null||qu.getTeacherId()!=userId){
            return Result.failed(ResultCode.PARAM_ERROR);
        }
        question.setCourseId(null);
        question.setTeacherId(null);
        questionService.updateById(question);
        return Result.msgSuccess("更新成功");
    }
    @Operation(summary = "删除题目")
    @PostMapping("/delete/{questionId}")
    public Result delete(@PathVariable String questionId){
        //判断题目是不是自己的
        Question qu = questionService.getById(questionId);
        Integer userId = UserAuthUtil.getUserId();
        if(qu==null||qu.getTeacherId()!=userId){
            return Result.failed(ResultCode.PARAM_ERROR);
        }
        questionService.removeById(questionId);
        return Result.msgSuccess("删除成功");
    }
    @Operation(summary = "题目列表")
    @GetMapping("/list/{courseId}")
    public Result list(
            @PathVariable Integer courseId,
            @RequestParam Integer currentPage,
            @RequestParam(required = false) Integer tagId){
        Courses course = courseClient.findCourse(courseId);
        Integer userId = UserAuthUtil.getUserId();
        if(course==null){
            return Result.failed(ResultCode.PARAM_ERROR);
        }
        // 不需要查分组是不是自己的，因为，下面查询时课程id 和分组id 同时成立才行
        LambdaQueryWrapper<Question> queryWrapper=new LambdaQueryWrapper<>();
        //老师
        Map<SFunction<Question, ?>, Object> queryMap=new HashMap<>();
        queryMap.put(Question::getTagId,tagId);
        queryMap.put(Question::getCourseId,courseId);
        if(course.getUserId()!=userId){
            //1.判断是否课程班级中
            JoinClass joinClass = courseClient.joinCourseByStuId(courseId, userId);
            if(joinClass==null){
                return Result.failed(ResultCode.PARAM_ERROR);
            }
            //TODO:判断该班级是否在考试，如果考试禁止获取
            //2.查找有公开题目的分类
            queryWrapper.gt(Question::getIsPublic,0);
        }
        queryWrapper.allEq(queryMap).orderByDesc(Question::getCreatedAt);
        IPage<Question> page=new Page<>(currentPage,10);
        IPage<Question> list = questionService.page(page,queryWrapper);
        return Result.success(PageResult.setResult(list));
    }
    @Operation(summary = "题目详情")
    @GetMapping("/detail/{questionId}")
    public Result<QuestionInfoVo> detail(
            @PathVariable Integer questionId){
        Question question = questionService.getById(questionId);
        Integer userId = UserAuthUtil.getUserId();
        // 是否有查看权限
        if(question.getTeacherId()!=userId){
            //1.判断是否课程班级中
            JoinClass joinClass = courseClient.joinCourseByStuId(question.getCourseId(), userId);
            if(joinClass==null){
                return Result.failed(ResultCode.PARAM_ERROR);
            }
            //TODO:判断该班级是否在考试，如果考试禁止获取

            //2.查找有公开题目的分类
        }
        QuestionInfoVo questionInfo = questionService.questionInfo(questionId);
        return Result.success(questionInfo);
    }
    
    @Operation(summary = "批量删除题目")
    @PostMapping("/batchDelete")
    public Result batchDelete(@RequestBody Map<String, List<Integer>> requestMap) {
        List<Integer> questionIds = requestMap.get("questionIds");
        if (questionIds == null || questionIds.isEmpty()) {
            return Result.failed(ResultCode.PARAM_ERROR, "题目ID列表不能为空");
        }
        
        Integer userId = UserAuthUtil.getUserId();
        
        // 检查所有题目是否都属于当前用户
        List<Question> questions = questionService.listByIds(questionIds);
        for (Question question : questions) {
            if (question.getTeacherId() != userId) {
                return Result.failed(ResultCode.PARAM_ERROR, "存在无权限操作的题目");
            }
        }
        
        // 执行批量删除
        boolean success = questionService.removeByIds(questionIds);
        if (success) {
            return Result.msgSuccess("批量删除成功");
        } else {
            return Result.msgError("批量删除失败");
        }
    }
    
    @Operation(summary = "批量更新题目可见性")
    @PostMapping("/batchUpdateVisibility")
    public Result batchUpdateVisibility(@RequestBody Map<String, Object> requestMap) {
        List<Integer> questionIds = (List<Integer>) requestMap.get("questionIds");
        String visibility = (String) requestMap.get("visibility");
        
        if (questionIds == null || questionIds.isEmpty()) {
            return Result.failed(ResultCode.PARAM_ERROR, "题目ID列表不能为空");
        }
        
        if (visibility == null || visibility.isEmpty()) {
            return Result.failed(ResultCode.PARAM_ERROR, "可见性参数不能为空");
        }
        
        // 转换可见性参数
        QuestionVisibleEnum visibleEnum;
        try {
            visibleEnum = QuestionVisibleEnum.valueOf(visibility);
        } catch (IllegalArgumentException e) {
            return Result.failed(ResultCode.PARAM_ERROR, "不支持的可见性类型");
        }
        
        Integer userId = UserAuthUtil.getUserId();
        
        // 检查所有题目是否都属于当前用户
        List<Question> questions = questionService.listByIds(questionIds);
        for (Question question : questions) {
            if (question.getTeacherId() != userId) {
                return Result.failed(ResultCode.PARAM_ERROR, "存在无权限操作的题目");
            }
        }
        
        // 执行批量更新
        boolean success = true;
        for (Integer questionId : questionIds) {
            Question question = new Question();
            question.setId(questionId);
            question.setIsPublic(visibleEnum);
            success = success && questionService.updateById(question);
        }
        
        if (success) {
            return Result.msgSuccess("批量更新可见性成功");
        } else {
            return Result.msgError("批量更新可见性失败");
        }
    }
}
