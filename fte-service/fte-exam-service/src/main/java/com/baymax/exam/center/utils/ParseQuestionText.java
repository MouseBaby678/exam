package com.baymax.exam.center.utils;

import com.baymax.exam.center.enums.QuestionTypeEnum;
import com.baymax.exam.center.enums.QuestionVisibleEnum;
import com.baymax.exam.center.model.ParseQuestionRules;
import com.baymax.exam.center.model.QuestionItem;
import com.baymax.exam.center.vo.QuestionInfoVo;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 方式一：正则表达
 *      缺点：不能正确提取内容，部分题目提取特征不明显
 * 方式二：逐行读取+正则表达
 *
 * @author ：Baymax
 * @date ：Created in 2022/11/10 16:52
 * @description：解析题目文本
 * @modified By：
 * @version:
 */
@Slf4j
public class ParseQuestionText {
    static final String[] letterList = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public static List<QuestionInfoVo> parse(String text, ParseQuestionRules rule){

        String divisionRule=rule.getDivisionRule();
        String questionRule=rule.getQuestionRule();
        String answerRule=rule.getAnswerRule();
        String optionRule=rule.getOptionRule();
        //1.把文本分割成单独的题目
        String[] questionInfoStr = text.split(divisionRule);
        //2.遍历题目文本
        QuestionInfoVo questionInfo;
        List<QuestionInfoVo> list=new ArrayList<>();
        QuestionTypeEnum type = QuestionTypeEnum.SIGNAL_CHOICE;

        for (String str : questionInfoStr) {
            if(str.isEmpty()){
               continue;
            }
            log.info("题目信息=>"+str);
            questionInfo=new QuestionInfoVo();
            
            // 识别难度、分值和可见度标记
            String metaPattern = "\\[难度:(\\d+)\\]\\s*\\[分值:(\\d+)\\]\\s*\\[可见:(self|course|overt)\\]";
            Pattern pattern1 = Pattern.compile(metaPattern);
            Matcher metaMatcher = pattern1.matcher(str);
            if(metaMatcher.find()) {
                try {
                    // 设置难度
                    int difficulty = Integer.parseInt(metaMatcher.group(1));
                    if(difficulty >= 0 && difficulty <= 5) {
                        questionInfo.setDifficulty(difficulty);
                    }
                    
                    // 设置分值
                    float score = Float.parseFloat(metaMatcher.group(2));
                    if(score >= 1 && score <= 100) {
                        questionInfo.setScore(score);
                    }
                    
                    // 设置可见性
                    String visibility = metaMatcher.group(3);
                    try {
                        questionInfo.setIsPublic(QuestionVisibleEnum.valueOf(visibility));
                    } catch (Exception e) {
                        // 默认设置为自己可见
                        questionInfo.setIsPublic(QuestionVisibleEnum.self);
                    }
                    
                    // 从原文本中移除元信息标记
                    str = str.replaceAll(metaPattern, "").trim();
                    log.info("移除标记后的题目信息=>" + str);
                } catch (Exception e) {
                    log.error("解析题目元信息时出错", e);
                }
            } else {
                // 设置默认值
                questionInfo.setDifficulty(3); // 默认中等难度
                questionInfo.setScore(5.0f);  // 默认5分
                questionInfo.setIsPublic(QuestionVisibleEnum.self); // 默认自己可见
            }
            
            Pattern pattern= Pattern.compile(answerRule);
            // 现在创建 matcher 对象
            //2.1 提取答案
            Matcher matcher = pattern.matcher(str);
            List<String>answerList=new ArrayList<>();
            if(matcher.find()){
                answerList=Arrays.stream(matcher.group(1).split(rule.getAnswerSplit())).collect(Collectors.toList());
            }
            log.info("答案=>"+answerList);
            //2.1.1 把答案去了
            str=str.replaceAll(answerRule,"");
            log.info("去答案后题目信息=>"+str);
            //2.2 提取选项
            List<String> optionList=Arrays.stream(str.split(optionRule)).collect(Collectors.toList());
            //2.3.提取题目
            questionInfo.setContent(optionList.get(0));
            log.info("题目=>"+optionList.get(0));
            optionList.remove(0);
            log.info("选项=>"+optionList);
            if(questionInfo.getContent().isBlank()){
                continue;
            }

            //2.4 确定题型
            if(optionList.isEmpty()){
                // 检查题目内容中是否包含下划线填空符
                boolean hasUnderscorePlaceholder = Pattern.compile("_{1,}").matcher(questionInfo.getContent()).find();
                
                if(hasUnderscorePlaceholder) {
                    // 如果包含下划线填空符，则认定为填空题
                    type = QuestionTypeEnum.COMPLETION;
                    log.info("检测到下划线填空符，识别为填空题");
                    
                    // 如果只有一个答案但有下划线，仍然视为填空题
                    if(answerList.size() == 1) {
                        // 创建与下划线数量相匹配的答案列表
                        Matcher underscoreMatcher = Pattern.compile("_{1,}").matcher(questionInfo.getContent());
                        List<String> newAnswerList = new ArrayList<>();
                        
                        // 如果下划线数量大于1，但答案只有1个，则用这一个答案填充所有空
                        while(underscoreMatcher.find()) {
                            newAnswerList.add(answerList.get(0));
                        }
                        
                        // 只有当实际找到了下划线时才替换
                        if(!newAnswerList.isEmpty()) {
                            answerList = newAnswerList;
                            log.info("根据下划线数量调整答案列表：" + answerList.size() + "个答案");
                        }
                    }
                } else {
                    // 无下划线时使用改进的逻辑
                    // 检查是否是主观题的特征
                    boolean isLikelySubjective = false;
                    
                    // 特征1: 答案文本较长(超过50个字符)通常是主观题
                    if (answerList.size() >= 1) {
                        String combinedAnswer = String.join("", answerList);
                        if (combinedAnswer.length() > 50) {
                            isLikelySubjective = true;
                            log.info("答案较长，可能是主观题");
                        }
                    }
                    
                    // 特征2: 题目内容包含"简述"、"论述"、"分析"等关键词
                    String questionContent = questionInfo.getContent().trim();
                    if (questionContent.contains("简述") || 
                        questionContent.contains("论述") || 
                        questionContent.contains("分析") ||
                        questionContent.contains("比较") ||
                        questionContent.contains("描述") ||
                        questionContent.contains("解释")) {
                        isLikelySubjective = true;
                        log.info("题目包含主观题关键词，识别为主观题");
                    }
                    
                    // 根据综合判断确定题型
                    if (isLikelySubjective || answerList.size() == 1) {
                        type = QuestionTypeEnum.SUBJECTIVE;
                        // 如果被识别为主观题但有多个答案，合并为一个
                        if (answerList.size() > 1) {
                            String combinedAnswer = String.join("；", answerList);
                            answerList = new ArrayList<>();
                            answerList.add(combinedAnswer);
                            log.info("合并主观题多个答案为一个");
                        }
                    } else {
                        type = QuestionTypeEnum.COMPLETION;
                    }
                }
            } else {
                if(answerList.size()==1){
                    if(optionList.size()==2){
                        type=QuestionTypeEnum.JUDGMENTAL;
                    }else{
                        type=QuestionTypeEnum.SIGNAL_CHOICE;
                    }
                }else{
                    type=QuestionTypeEnum.MULTIPLE_CHOICE;
                }
            }
            questionInfo.setType(type);
            //2.5获取题目选项
            questionInfo.setOptions(getItem(type,answerList,optionList));
            list.add(questionInfo);
        }
        return list;
    }
    public static List<QuestionInfoVo> lineParse(String text, ParseQuestionRules rule) throws IOException {
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
        String buffer;
        List<QuestionInfoVo> list=new ArrayList<>();
        int preStep=0;
        String tempStr;
        QuestionInfoVo questionInfo;
        List<QuestionItem> questionItems;
        QuestionTypeEnum type;
        while ((buffer=bufferedReader.readLine())!=null){
            questionInfo=new QuestionInfoVo();
            switch (preStep){
                //啥也没匹配到，折为上一步的内容
                case 1:
                    tempStr=questionInfo.getContent();
                    questionInfo.setContent(tempStr+buffer);
                case 2:
                case 3:

            }
            if(buffer.matches(rule.getQuestionRule())){
                //当匹配的下一个题目的时候添加到
                preStep=1;
                questionInfo.setContent(buffer);
                questionItems=new ArrayList<>();
//                continue;如果题目中包含答案的模式吧
            }
            if(buffer.matches(rule.getQuestionRule())){
                preStep=2;
//                continue;
            }
            if(buffer.matches(rule.getAnswerRule())){
                preStep=3;
//                continue;
            }

        }
        return list;
    }
    private static List<QuestionItem> getItem(QuestionTypeEnum type,List<String> answerList,List<String> optionList){
        List<QuestionItem> list=null;
        //选择填空
        if(type==QuestionTypeEnum.SUBJECTIVE||type==QuestionTypeEnum.COMPLETION){
            list=answerList.stream().filter(i->!i.isBlank()).map(i->{
                QuestionItem item=new QuestionItem();
                item.setAnswer(i);
                return item;
            }).collect(Collectors.toList());
        //单选多选
        }else if(type==QuestionTypeEnum.SIGNAL_CHOICE||type== QuestionTypeEnum.MULTIPLE_CHOICE){
            List<String> temAnswerList=answerList.stream().map(e -> {
                return e.replaceAll("\\r|\\n|\\s", "").toUpperCase();
            }).collect(Collectors.toList());
            //获取字母的下标
            list=IntStream.rangeClosed(0,optionList.size()-1).filter(i->!optionList.get(i).isBlank()).mapToObj(i->{
                QuestionItem item=new QuestionItem();
                item.setContent(optionList.get(i));
                //确定选项答案
                boolean contains = temAnswerList.contains(letterList[i]);
                if(contains){
                    log.error("正确答案");
                    item.setAnswer("1");
                }
                return item;
            }).collect(Collectors.toList());
        }else if(type==QuestionTypeEnum.JUDGMENTAL){

        }
        return list;
    }

}

