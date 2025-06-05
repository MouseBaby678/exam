package com.baymax.exam.center.autopaper;

import com.baymax.exam.center.model.Question;
import com.baymax.exam.center.vo.AutomaticPaperRuleVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description：试卷个体 - 优化版
 * @modified By：
 * @version: 2.0
 */
@Slf4j
@Data
public class Paper {
    private int id;

    /**
     * 适应度 - 评价试卷优劣的指标，越高越好
     */
    private double adaptationDegree = 0;

    /**
     * 知识点覆盖率 - 试卷覆盖的知识点比例
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
     * 试卷覆盖的知识点集合
     */
    private Set<Integer> coveredTags = new HashSet<>();

    /**
     * 个体包含的试题集合
     */
    private List<Question> questionList = new ArrayList<>();

    /**
     * 计算并返回试卷总分
     */
    public double getTotalScore() {
        if (totalScore == 0) {
            totalScore = questionList.stream()
                    .mapToDouble(Question::getScore)
                    .sum();
            log.trace("试卷ID={}计算总分：{}", id, totalScore);
        }
        return totalScore;
    }

    /**
     * 计算并返回试卷难度系数
     * 使用加权平均计算试卷整体难度
     */
    public double getDifficulty() {
        if (difficulty == 0) {
            if (questionList.isEmpty() || getTotalScore() == 0) {
                difficulty = 0;
                log.trace("试卷ID={}为空或总分为0，难度系数设为0", id);
            } else {
                difficulty = questionList.stream()
                        .mapToDouble(question -> question.getScore() * question.getDifficulty())
                        .sum() / getTotalScore();
                log.trace("试卷ID={}计算难度系数：{}", id, difficulty);
            }
        }
        return difficulty;
    }

    /**
     * 计算试卷知识点覆盖率
     *
     * @param targetTags 目标知识点集合
     */
    public void setTagCoverage(Set<Integer> targetTags) {
        if (tagCoverage == 0) {
            // 先计算试卷涵盖的知识点
            coveredTags = questionList.stream()
                    .map(Question::getTagId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (targetTags == null || targetTags.isEmpty()) {
                // 如果目标知识点为空，设置默认覆盖率
                tagCoverage = 0.5;
                log.trace("试卷ID={}目标知识点为空，设置默认知识点覆盖率：{}", id, tagCoverage);
            } else {
                // 计算交集大小
                Set<Integer> intersection = new HashSet<>(targetTags);
                intersection.retainAll(coveredTags);

                // 计算覆盖率
                tagCoverage = (double) intersection.size() / targetTags.size();
                log.trace("试卷ID={}计算知识点覆盖率：{}，覆盖{}/{}个知识点",
                        id, tagCoverage, intersection.size(), targetTags.size());
            }
        }
    }

    /**
     * 直接设置知识点覆盖率值
     *
     * @param coverageValue 知识点覆盖率值
     */
    public void setTagCoverageValue(double coverageValue) {
        this.tagCoverage = coverageValue;
    }

    /**
     * 计算试卷适应度
     * 综合考虑知识点覆盖率和难度匹配度
     *
     * @param rule 组卷规则
     * @param tagWeight 知识点权重
     * @param difficultyWeight 难度权重
     */
    public void setAdaptationDegree(AutomaticPaperRuleVo rule, double tagWeight, double difficultyWeight) {
        if (adaptationDegree == 0) {
            // 获取知识点覆盖率得分
            double tagCoverageScore = getTagCoverage() * tagWeight;

            // 计算难度匹配得分 (0-1之间)
            double difficultyScore = 0;

            // 防止除零错误并处理难度规则
            if (rule.getDifficulty() != null && rule.getDifficulty() > 0) {
                // 计算难度差异百分比
                double difficultyDiff = Math.abs(rule.getDifficulty() - getDifficulty());

                // 规范化难度差异到0-1范围（差异越小越好）
                // 使用指数衰减函数，使得接近目标难度时得分迅速提高
                difficultyScore = Math.exp(-2 * difficultyDiff) * difficultyWeight;

                log.trace("试卷ID={}难度差异：{}，难度得分：{}", id, difficultyDiff, difficultyScore);
            }

            // 题型分布得分 - 确保不同题型的比例合理
            double typeDistributionScore = 0;
            if (rule.getQuestionType() != null && !rule.getQuestionType().isEmpty() &&
                rule.getPercentage() != null && !rule.getPercentage().isEmpty()) {
                // 这里可以添加题型分布评分代码
                // 暂时略过，未来可以扩展
            }

            // 总得分为各部分之和，确保在0-1范围内
            double newAdaptationDegree = Math.min(1.0, Math.max(0.0, tagCoverageScore + difficultyScore + typeDistributionScore));
            adaptationDegree = newAdaptationDegree;

            log.debug("试卷ID={}计算适应度：{}，知识点覆盖率={}, 知识点得分={}, 难度系数={}, 目标难度={}, 难度得分={}",
                    id, adaptationDegree, getTagCoverage(), tagCoverageScore,
                    getDifficulty(), rule.getDifficulty(), difficultyScore);
        }
    }

    /**
     * 直接设置适应度值
     *
     * @param degreeValue 适应度值
     */
    public void setAdaptationDegreeValue(double degreeValue) {
        this.adaptationDegree = degreeValue;
    }

    /**
     * 检查试卷是否包含指定题目
     *
     * @param question 待检查的题目
     * @return 是否包含
     */
    public boolean containsQuestion(Question question) {
        if (question == null || question.getId() == null) {
            return false;
        }

        boolean contains = questionList.stream()
                .anyMatch(q -> q != null && Objects.equals(question.getId(), q.getId()));

        if (contains) {
            log.trace("试卷ID={}已包含题目ID={}", id, question.getId());
        }

        return contains;
    }

    /**
     * 替换指定位置的题目
     *
     * @param index 位置索引
     * @param question 新题目
     */
    public void saveQuestion(int index, Question question) {
        if (index < 0 || index >= questionList.size()) {
            log.warn("试卷ID={}替换题目失败：索引{}超出范围[0,{})", id, index, questionList.size());
            return;
        }

        Question oldQuestion = questionList.get(index);
        log.trace("试卷ID={}替换位置{}的题目：旧题目ID={}，新题目ID={}", id, index,
                oldQuestion != null ? oldQuestion.getId() : "null", question.getId());

        questionList.set(index, question);
        clearDataCache();
    }

    /**
     * 添加题目到试卷
     *
     * @param question 待添加的题目
     */
    public void addQuestion(Question question) {
        if (question == null) {
            log.warn("试卷ID={}添加题目失败：题目为null", id);
            return;
        }

        questionList.add(question);
        log.trace("试卷ID={}添加题目：ID={}，当前题目数量={}", id, question.getId(), questionList.size());
        clearDataCache();
    }

    /**
     * 获取指定位置的题目
     *
     * @param index 位置索引
     * @return 题目对象
     */
    public Question getQuestion(int index) {
        if (index < 0 || index >= questionList.size()) {
            log.warn("试卷ID={}获取题目失败：索引{}超出范围[0,{})", id, index, questionList.size());
            return null;
        }
        return questionList.get(index);
    }

    /**
     * 获取试卷中题目数量
     *
     * @return 题目数量
     */
    public int getQuestionSize() {
        return questionList.size();
    }

    /**
     * 从试卷中移除指定位置的题目
     *
     * @param index 位置索引
     * @return 被移除的题目
     */
    public Question removeQuestion(int index) {
        if (index < 0 || index >= questionList.size()) {
            log.warn("试卷ID={}移除题目失败：索引{}超出范围[0,{})", id, index, questionList.size());
            return null;
        }

        Question removed = questionList.remove(index);
        log.trace("试卷ID={}移除位置{}的题目：ID={}", id, index,
                removed != null ? removed.getId() : "null");

        clearDataCache();
        return removed;
    }

    /**
     * 清除计算缓存
     * 当试卷内容发生变化时调用此方法重置缓存数据
     */
    public void clearDataCache() {
        totalScore = 0;
        adaptationDegree = 0;
        difficulty = 0;
        tagCoverage = 0;
        coveredTags.clear();
        log.trace("试卷ID={}清除缓存数据", id);
    }

    /**
     * 深度复制当前试卷
     *
     * @return 试卷的副本
     */
    public Paper deepCopy() {
        Paper copy = new Paper();
        copy.setId(this.id);
        copy.getQuestionList().addAll(this.questionList);
        copy.setDifficulty(this.difficulty);
        copy.setTagCoverageValue(this.tagCoverage);
        copy.setAdaptationDegreeValue(this.adaptationDegree);
        copy.setTotalScore(this.totalScore);
        copy.getCoveredTags().addAll(this.coveredTags);
        return copy;
    }
}

