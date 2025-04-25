package com.baymax.exam.center.service.impl;

import com.baymax.exam.center.autopaper.AutomaticPaperConfig;
import com.baymax.exam.center.autopaper.Paper;
import com.baymax.exam.center.autopaper.Population;
import com.baymax.exam.center.enums.QuestionTypeEnum;
import com.baymax.exam.center.model.Question;
import com.baymax.exam.center.vo.AutomaticPaperRuleVo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ：Baymax
 * @date ：Created in 2023/3/11 15:44
 * @description：
 * @modified By：
 * @version:
 */
@Slf4j
public class GeneratePaperService {
    private List<Question> questions;
    private AutomaticPaperRuleVo rule;

    private AutomaticPaperConfig config;

    /**
     * 生成论文服务
     *
     * @param questions 问题
     * @param rule      规则
     */
    public GeneratePaperService(List<Question> questions, AutomaticPaperRuleVo rule){
        this.questions=questions;
        this.rule=rule;
        config=AutomaticPaperConfig.getConfig(rule.getTotalNumber(),questions.size());
        log.info("初始化组卷配置: 种群大小={}, 最大迭代次数={}, 交叉率={}, 变异率={}, 目标适应度={}",
                config.getPopulationSize(), config.getMaxGeneration(), 
                config.getCrossoverRate(), config.getMutationRate(), config.getTargetExpand());
    }

    public Paper generatePaper() {
        // 检查题库中是否有题目
        if (questions.isEmpty()) {
            log.warn("题库为空，返回空试卷");
            // 返回一个空的试卷对象
            Paper emptyPaper = new Paper();
            emptyPaper.setAdaptationDegree(0);
            emptyPaper.setDifficulty(0);
            emptyPaper.setTagCoverage(rule.getTags());
            return emptyPaper;
        }
        
        log.info("开始组卷，题库中题目数量: {}, 需要选择题目数量: {}", questions.size(), rule.getTotalNumber());
        
        // 校验题型是否存在
        if (rule.getQuestionType() != null && !rule.getQuestionType().isEmpty()) {
            // 获取所有题目的题型
            Set<QuestionTypeEnum> availableTypes = questions.stream()
                    .map(Question::getType)
                    .collect(Collectors.toSet());
                    
            // 过滤出题库中存在的题型
            List<QuestionTypeEnum> validTypes = rule.getQuestionType().stream()
                    .filter(availableTypes::contains)
                    .collect(Collectors.toList());
                    
            // 如果没有有效题型，但有题目，使用题库中的题型
            if (validTypes.isEmpty() && !availableTypes.isEmpty()) {
                log.info("未找到规则中指定的有效题型，使用题库中所有题型: {}", availableTypes);
                rule.setQuestionType(new ArrayList<>(availableTypes));
            } else if (!validTypes.isEmpty()) {
                log.info("使用题库中存在的有效题型: {}", validTypes);
                rule.setQuestionType(validTypes);
            }
        }
        
        // 创建种群
        try {
            log.info("开始创建初始种群");
            Population population = new Population(config.getPopulationSize(), questions, rule);
            log.info("初始种群创建完成，种群大小: {}", population.getLength());
            // 迭代计数器
            int count = 0;
            Paper bastPaper;
            // 记录最好适应度
            double bestAdaptation = -1.0;
            int noImproveCount = 0; // 连续没有改进的次数
            
            log.info("开始遗传算法迭代过程");
            do {
                count++;
                log.info("第 {} 次迭代开始", count);
                
                // 记录迭代开始时间
                long startTime = System.currentTimeMillis();
                
                population = evolvePopulation(population);
                bastPaper = population.getFitness();
                
                // 计算迭代耗时
                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;
                
                // 检查是否有改进
                boolean improved = false;
                if (bastPaper.getAdaptationDegree() > bestAdaptation) {
                    improved = true;
                    bestAdaptation = bastPaper.getAdaptationDegree();
                    noImproveCount = 0;
                } else {
                    noImproveCount++;
                }
                
                log.info("第 {} 次迭代完成，耗时: {}ms，当前最佳适应度: {}, 题目数量: {}, 难度系数: {}, 是否有改进: {}", 
                        count, timeElapsed, bastPaper.getAdaptationDegree(), 
                        bastPaper.getQuestionSize(), bastPaper.getDifficulty(), improved);
                
                if (count % 5 == 0 || improved) {
                    log.info("第 {} 次迭代，最佳试卷知识点覆盖率: {}", count, bastPaper.getTagCoverage());
                }
                
                // 如果连续10次没有改进，可以提前终止
                if (noImproveCount >= 10 && count >= 15) {
                    log.info("连续{}次迭代没有改进，提前终止迭代", noImproveCount);
                    break;
                }
            } while (count < config.getMaxGeneration() && bastPaper.getAdaptationDegree() < config.getTargetExpand());
            
            log.info("遗传算法迭代结束，共迭代 {} 次，最终最佳适应度: {}", count, bastPaper.getAdaptationDegree());
            return population.getFitness();
        } catch (Exception e) {
            log.error("遗传算法执行异常，回退到随机选题: {}", e.getMessage(), e);
            // 发生异常时，返回一个基本试卷（随机选取题目）
            Paper fallbackPaper = new Paper();
            int limit = Math.min(rule.getTotalNumber(), questions.size());
            Collections.shuffle(questions);
            for (int i = 0; i < limit; i++) {
                fallbackPaper.addQuestion(questions.get(i));
            }
            fallbackPaper.setTagCoverage(rule.getTags());
            fallbackPaper.setAdaptationDegree(rule, AutomaticPaperConfig.TAG_WEIGHT, AutomaticPaperConfig.DIFFICULTY_WEIGHT);
            log.info("随机选题完成，适应度: {}", fallbackPaper.getAdaptationDegree());
            return fallbackPaper;
        }
    }

    private Population evolvePopulation(Population population) {
        //新总群
        Population nextPopulation=new Population(config.getPopulationSize());
        if(config.isElitism()){
            Paper elitePaper = population.getFitness();
            nextPopulation.addPaper(elitePaper);
            log.debug("保留精英个体，适应度: {}", elitePaper.getAdaptationDegree());
        }
        //交叉
        log.debug("开始种群交叉操作");
        int initIndex=nextPopulation.getPaperIndex();
        for(;initIndex<config.getPopulationSize();initIndex++){
            // 较优选择parent
            Paper parent1 = select(population);
            Paper parent2 = select(population);
            while (parent2.getId() == parent1.getId()) {
                parent2 = select(population);
            }
            // 交叉
            Paper child = crossover(parent1, parent2);
            nextPopulation.addPaper(child);
            log.trace("交叉操作: 父代1(ID={}, 适应度={}), 父代2(ID={}, 适应度={}), 产生子代", 
                    parent1.getId(), parent1.getAdaptationDegree(), 
                    parent2.getId(), parent2.getAdaptationDegree());
        }
        
        log.debug("交叉操作完成，开始变异操作");
        initIndex=nextPopulation.getPaperIndex();
        // 种群变异操作
        Paper tmpPaper;
        int mutationCount = 0;
        for (; initIndex<config.getPopulationSize();initIndex++) {
            tmpPaper = nextPopulation.getPaper(initIndex);
            int beforeMutation = tmpPaper.getQuestionList().size();
            mutate(tmpPaper);
            int afterMutation = tmpPaper.getQuestionList().size();
            if (beforeMutation != afterMutation) {
                mutationCount++;
            }
            // 计算知识点覆盖率与适应度
            tmpPaper.setTagCoverage(rule.getTags());
            tmpPaper.setAdaptationDegree(rule, AutomaticPaperConfig.TAG_WEIGHT, AutomaticPaperConfig.DIFFICULTY_WEIGHT);
        }
        log.debug("变异操作完成，共有 {} 个个体发生变异", mutationCount);
        
        Paper bestPaper = nextPopulation.getFitness();
        log.debug("本次进化后种群的最佳适应度: {}", bestPaper.getAdaptationDegree());
        
        return nextPopulation;
    }

    /**
     * 选择操作：根据适应度值，采用某种选择策略（如轮盘赌法、锦标赛法等），从当前种群中选择一定数量的个体进入下一代。
     * @param population 种群
     * @return {@link Paper}
     */
    private Paper select(Population population) {
        // 检查种群大小，确保不会出现死循环
        int tournamentSize = Math.min(config.getTournamentSize(), population.getLength());
        if (tournamentSize < 1) {
            // 如果没有足够的个体，直接返回第一个
            log.warn("种群大小不足以进行锦标赛选择，直接返回第一个个体");
            return population.getPaper(0);
        }
        
        // 正常的锦标赛选择
        Population childPopulation = population.getChildPopulation(tournamentSize);
        Paper selectedPaper = childPopulation.getFitness();
        log.trace("选择操作: 从{}个候选者中选择适应度为{}的个体", tournamentSize, selectedPaper.getAdaptationDegree());
        return selectedPaper;
    }

    /**
     * 交叉操作：根据交叉概率，在选择出来的个体中随机配对，并在某一位置进行基因交换，产生新的子代。
     *
     * @param currentPaper 当前纸
     * @param nextPaper    下一个纸
     * @return {@link Paper}
     */
    private Paper crossover(Paper currentPaper, Paper nextPaper) {
        Paper paper = new Paper();
        int retainNumber = (int) (config.getCrossoverRate() * currentPaper.getQuestionSize());
        //保留个体
        int index;
        for(index=0; index<retainNumber && index<currentPaper.getQuestionSize(); index++){
            paper.addQuestion(currentPaper.getQuestion(index));
        }
        
        //个体交叉
        int maxAttempts = questions.size() * 2; // 设置最大尝试次数，避免无限循环
        int attempts = 0;
        
        for(index=retainNumber; index<nextPaper.getQuestionSize(); index++){
            Question question = nextPaper.getQuestion(index);
            
            // 如果试题已存在，尝试从题库中随机获取新试题
            if(paper.containsQuestion(question)){
                // 防止无限循环
                boolean found = false;
                attempts = 0;
                
                while(!found && attempts < maxAttempts) {
                    attempts++;
                    question = getRandomQuestion();
                    
                    // 如果找到一个未包含的题目或者已经尝试了足够多次，则退出循环
                    if(!paper.containsQuestion(question) || attempts >= maxAttempts) {
                        found = true;
                    }
                }
                
                // 如果超过最大尝试次数仍未找到，记录日志并跳过此题
                if(attempts >= maxAttempts) {
                    log.warn("交叉操作: 无法找到不重复的题目，尝试次数：{}，跳过此题", attempts);
                    continue;
                }
            }
            
            paper.addQuestion(question);
            log.trace("交叉操作: 添加题目ID={}", question.getId());
        }
        
        return paper;
    }

    /**
     * 变异
     * 变异操作：根据变异概率，在子代中随机选取若干位进行基因翻转，产生新的变异子代。
     * @param paper 试卷
     */
    private void mutate(Paper paper) {
        int mutatedCount = 0;
        int maxAttempts = questions.size(); // 最大尝试次数
        
        for (int i = 0; i < paper.getQuestionList().size(); i++) {
            if(Math.random() < config.getMutationRate()){
                Question oldQuestion = paper.getQuestion(i);
                Question newQuestion;
                
                // 尝试找到一个不在试卷中的新题目
                int attempts = 0;
                boolean found = false;
                
                while(!found && attempts < maxAttempts) {
                    attempts++;
                    newQuestion = getRandomQuestion();
                    
                    // 如果找到不重复的题目或尝试次数已达上限，则退出循环
                    if(!paper.containsQuestion(newQuestion) || attempts >= maxAttempts) {
                        found = true;
                        
                        if(!paper.containsQuestion(newQuestion)) {
                            paper.saveQuestion(i, newQuestion);
                            mutatedCount++;
                            log.trace("题目变异: 位置={}, 旧题目ID={}, 新题目ID={}", 
                                    i, oldQuestion.getId(), newQuestion.getId());
                        } else {
                            log.trace("题目变异: 无法找到不重复的题目，保持原题，位置={}, 题目ID={}", 
                                    i, oldQuestion.getId());
                        }
                    }
                }
            }
        }
        
        if (mutatedCount > 0) {
            log.trace("试卷变异完成，共变异 {} 道题目", mutatedCount);
        } else {
            log.trace("试卷无变异发生");
        }
    }
    
    private Question getRandomQuestion(){
        int randomIndex = (int) (Math.random() * questions.size());
        return questions.get(randomIndex);
    }
}
