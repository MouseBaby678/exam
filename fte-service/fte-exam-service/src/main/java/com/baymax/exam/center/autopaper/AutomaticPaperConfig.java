package com.baymax.exam.center.autopaper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：Baymax
 * @date ：Created in 2023/3/12 18:23
 * @description：自动组卷配置类
 * @modified By：
 * @version:
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class AutomaticPaperConfig {
    private double targetExpand=0.98;
    //定义基本参数
    private int populationSize = 20; //种群规模
    private double crossoverRate = 0.8; //交叉概率
    private double mutationRate = 0.01; //变异概率
    private int maxGeneration = 50; //最大迭代次数
    private boolean elitism=true;//精英主义

    public static double TAG_WEIGHT = 0.20;//知识点权重

    public static double DIFFICULTY_WEIGHT = 0.80;//难度权重
    private int tournamentSize=5;

    // 超小规模配置 - 适用于题库很小或选题数量很少的情况
    public void setMicroScale() {
        targetExpand=0.7;
        populationSize=10;
        maxGeneration=20;
        mutationRate=0.3;
        crossoverRate=0.7; 
        tournamentSize=3;
    }
    
    public void setSmallScare(){
        targetExpand=0.8;
        populationSize=20;
        maxGeneration=30;
        mutationRate=0.5;
        crossoverRate=0.8;
    }
    
    public void setMediumScale(){
        targetExpand=0.85;
        populationSize=40;
        maxGeneration=40;
        mutationRate=0.6;
        crossoverRate=0.85;
    }
    
    public void setLargeScale(){
        targetExpand=0.9;
        populationSize=60;
        maxGeneration=60;
        mutationRate=0.7;
        crossoverRate=0.9;
    }

    /**
     * 根据题量的占比，更改自动组卷的配置，占比越小，迭代次数小，但变异等概率将变大
     *
     * @param desiredNumber  期望数量
     * @param questionNumber 问题数量
     * @return {@link AutomaticPaperConfig}
     */
    public static AutomaticPaperConfig getConfig(int desiredNumber, int questionNumber){
        AutomaticPaperConfig config=new AutomaticPaperConfig();
        
        // 题库和选题都很小的情况，优先使用微型配置
        if (desiredNumber <= 5 && questionNumber <= 20) {
            config.setMicroScale();
            log.info("题库较小({}题)且选题数较少({}题)，使用微型配置", questionNumber, desiredNumber);
            return config;
        }
        
        // 使用比例而不是相除，防止整数除法问题
        double ratio = (double) questionNumber / desiredNumber;
        log.info("题库/选题比例: {}/{} = {}", questionNumber, desiredNumber, ratio);
        
        if (ratio > 10) {
            config.setSmallScare();
            log.info("题库/选题比例 > 10, 使用小规模配置");
        } else if (ratio > 5) {
            config.setMediumScale();
            log.info("题库/选题比例 > 5, 使用中等规模配置");
        } else {
            config.setLargeScale();
            log.info("题库/选题比例 <= 5, 使用大规模配置");
        }
        
        return config;
    }
}
