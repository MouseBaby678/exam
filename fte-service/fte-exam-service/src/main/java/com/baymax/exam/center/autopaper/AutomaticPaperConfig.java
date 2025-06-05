package com.baymax.exam.center.autopaper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description：自动组卷配置类 - 优化版
 * @modified By：
 * @version: 2.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class AutomaticPaperConfig {
    // 期望达到的目标适应度值
    private double targetExpand = 0.98;

    // 遗传算法基本参数
    private int populationSize = 30; // 种群规模
    private double crossoverRate = 0.85; // 交叉概率
    private double mutationRate = 0.05; // 变异概率
    private int maxGeneration = 60; // 最大迭代次数
    private boolean elitism = true; // 精英保留策略

    // 适应度计算权重
    public static double TAG_WEIGHT = 0.30; // 知识点权重
    public static double DIFFICULTY_WEIGHT = 0.70; // 难度权重

    // 锦标赛选择参数
    private int tournamentSize = 5;

    // 提前终止参数
    private int maxNoImproveCount = 10; // 最大允许连续不改进次数
    private int minGeneration = 15; // 最小迭代次数

    /**
     * 极小规模配置 - 适用于题库极小或选题数量极少的情况
     */
    public void setMicroScale() {
        targetExpand = 0.75;
        populationSize = 10;
        maxGeneration = 20;
        mutationRate = 0.25;
        crossoverRate = 0.75;
        tournamentSize = 3;
        maxNoImproveCount = 8;
        minGeneration = 10;
        log.info("应用极小规模配置");
    }

    /**
     * 小规模配置 - 适用于题库较小或选题数量较少的情况
     */
    public void setSmallScale() {
        targetExpand = 0.85;
        populationSize = 20;
        maxGeneration = 35;
        mutationRate = 0.15;
        crossoverRate = 0.80;
        tournamentSize = 4;
        maxNoImproveCount = 10;
        minGeneration = 15;
        log.info("应用小规模配置");
    }

    /**
     * 中等规模配置 - 适用于题库和选题数量适中的情况
     */
    public void setMediumScale() {
        targetExpand = 0.90;
        populationSize = 40;
        maxGeneration = 50;
        mutationRate = 0.10;
        crossoverRate = 0.85;
        tournamentSize = 5;
        maxNoImproveCount = 12;
        minGeneration = 20;
        log.info("应用中等规模配置");
    }

    /**
     * 大规模配置 - 适用于题库很大或选题数量很多的情况
     */
    public void setLargeScale() {
        targetExpand = 0.95;
        populationSize = 60;
        maxGeneration = 70;
        mutationRate = 0.05;
        crossoverRate = 0.90;
        tournamentSize = 6;
        maxNoImproveCount = 15;
        minGeneration = 25;
        log.info("应用大规模配置");
    }

    /**
     * 根据题量的占比，更改自动组卷的配置
     * 题库越大，可能的组合就越多，需要更大的种群和更多的迭代
     *
     * @param desiredNumber  期望选择的题目数量
     * @param questionNumber 题库中的题目总数
     * @return {@link AutomaticPaperConfig}
     */
    public static AutomaticPaperConfig getConfig(int desiredNumber, int questionNumber) {
        AutomaticPaperConfig config = new AutomaticPaperConfig();

        // 题库和选题都很小的特殊情况处理
        if (desiredNumber <= 5 || questionNumber <= 15) {
            config.setMicroScale();
            log.info("题库较小({}题)或选题数较少({}题)，使用极小规模配置", questionNumber, desiredNumber);
            return config;
        }

        // 计算题库与选题的比例
        double ratio = (double) questionNumber / desiredNumber;
        log.info("题库/选题比例: {}/{} = {}", questionNumber, desiredNumber, ratio);

        // 根据比例选择适当的配置
        if (ratio < 3) {
            config.setLargeScale();
            log.info("题库/选题比例 < 3，使用大规模配置（题目选择空间较小，需要更多迭代寻找最优解）");
        } else if (ratio < 7) {
            config.setMediumScale();
            log.info("题库/选题比例在3-7之间，使用中等规模配置");
        } else if (ratio < 15) {
            config.setSmallScale();
            log.info("题库/选题比例在7-15之间，使用小规模配置");
        } else {
            // 题库非常大，选题空间充足，使用微型配置即可快速找到较好解
            config.setMicroScale();
            log.info("题库/选题比例 >= 15，使用极小规模配置（题目选择空间充足）");
        }

        return config;
    }
}
