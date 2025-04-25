package com.baymax.exam.center.autopaper;

import com.baymax.exam.center.model.Question;
import com.baymax.exam.center.vo.AutomaticPaperRuleVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ：Baymax
 * @date ：Created in 2023/3/11 10:15
 * @description：试卷个体
 * @modified By：
 * @version:
 */
@Slf4j
@Data
public class Paper {
    private  int id;
    /**
     * 适应度
     */
    private double adaptationDegree = 0;
    /**
     * 知识点覆盖率
     */
    private double tagCoverage = 0;
    /**
     * 试卷总分
     */
    private double totalScore = 0;
    /**
     * 试卷难度系数
     */
    private double difficulty = 0;
    /**
     * 个体包含的试题集合
     */
    private List<Question> questionList = new LinkedList<>();

    public double getTotalScore() {
        if(totalScore==0){
            totalScore=questionList.stream().mapToDouble(Question::getScore).sum();
            log.trace("试卷ID={}计算总分：{}", id, totalScore);
        }
        return totalScore;
    }

    public double getDifficulty() {
        if(difficulty==0){
            difficulty=questionList.stream().mapToDouble(question->question.getScore()*question.getDifficulty()).sum()/getTotalScore();
            log.trace("试卷ID={}计算难度系数：{}", id, difficulty);
        }
        return difficulty;
    }

    public void setTagCoverage(Set<Integer> tags) {
        if(tagCoverage==0&&!tags.isEmpty()){
            Set<Integer> result = new HashSet<>(tags);
            Set<Integer> another = questionList.stream().map(Question::getTagId).collect(Collectors.toSet());
            // 交集操作
            result.retainAll(another);
            tagCoverage =(double) result.size() / tags.size();
            log.trace("试卷ID={}计算知识点覆盖率：{}，覆盖{}/{}个知识点", id, tagCoverage, result.size(), tags.size());
        }else{
            tagCoverage=0.5;
            log.trace("试卷ID={}知识点为空，设置默认知识点覆盖率：{}", id, tagCoverage);
        }
    }

    public void setAdaptationDegree(AutomaticPaperRuleVo rule, double f1, double f2) {
        if (adaptationDegree == 0) {
            // 获取知识点覆盖率得分
            double tagCoverageScore = getTagCoverage() * f1;
            
            // 计算难度匹配得分 (0-1之间)
            double difficultyScore = 0;
            // 防止除零错误
            if (rule.getDifficulty() != null && rule.getDifficulty() > 0) {
                // 计算难度差异百分比
                double difficultyDiff = Math.abs(rule.getDifficulty() - getDifficulty());
                // 规范化难度值到5.0以内
                double normalizedDiff = Math.min(difficultyDiff, 5.0) / 5.0;
                // 将难度差异转换为得分 (差异越小得分越高)
                difficultyScore = (1.0 - normalizedDiff) * f2;
            }
            
            // 总得分为两部分之和，确保在0-1范围内
            double newAdaptationDegree = Math.min(1.0, Math.max(0.0, tagCoverageScore + difficultyScore));
            adaptationDegree = newAdaptationDegree;
            
            log.debug("试卷ID={}计算适应度：{}，知识点覆盖率={}, 知识点得分={}, 难度系数={}, 目标难度={}, 难度得分={}, 权重f1={}, f2={}", 
                    id, adaptationDegree, getTagCoverage(), tagCoverageScore, 
                    getDifficulty(), rule.getDifficulty(), difficultyScore, f1, f2);
        }
    }
    
    public boolean containsQuestion(Question question) {
        boolean contains = questionList.stream().anyMatch(q-> q==null || Objects.equals(question.getId(), q.getId()));
        if (contains) {
            log.trace("试卷ID={}已包含题目ID={}", id, question.getId());
        }
        return contains;
    }

    /**
     * 增加问题
     *
     * @param question
     */
    public void saveQuestion(int index, Question question) {
        Question oldQuestion = questionList.get(index);
        log.trace("试卷ID={}替换位置{}的题目：旧题目ID={}，新题目ID={}", id, index, 
                oldQuestion != null ? oldQuestion.getId() : "null", question.getId());
        questionList.set(index, question);
        clearDataCache();
    }

    /**
     * 添加问题
     *
     * @param question 问题
     */
    public void addQuestion(Question question) {
        questionList.add(question);
        log.trace("试卷ID={}添加题目：ID={}，当前题目数量={}", id, question.getId(), questionList.size());
        clearDataCache();
    }

    /**
     * 获取题目
     *
     * @param index 指数
     * @return {@link Question}
     */
    public Question getQuestion(int index) {
        return questionList.get(index);
    }

    /**
     * 得到问题大小
     *
     * @return int
     */
    public int getQuestionSize() {
        return questionList.size();
    }

    /**
     * 清除数据缓存
     */
    public void clearDataCache(){
        totalScore = 0;
        adaptationDegree = 0;
        difficulty = 0;
        tagCoverage = 0;
        log.trace("试卷ID={}清除缓存数据", id);
    }
}
