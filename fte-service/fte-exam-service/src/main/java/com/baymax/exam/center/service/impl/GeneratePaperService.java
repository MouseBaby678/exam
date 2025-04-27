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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author ：Baymax
 * @date ：Created in 2023/3/11 15:44
 * @description：试卷生成服务 - 优化版
 * @modified By：
 * @version: 2.0
 */
@Slf4j
public class GeneratePaperService {
    // 候选题目列表
    private final List<Question> questions;
    
    // 组卷规则
    private final AutomaticPaperRuleVo rule;
    
    // 遗传算法配置
    private final AutomaticPaperConfig config;
    
    // 随机数生成器
    private final Random random = ThreadLocalRandom.current();

    /**
     * 构造方法
     * 
     * @param questions 候选题目列表
     * @param rule 组卷规则
     */
    public GeneratePaperService(List<Question> questions, AutomaticPaperRuleVo rule) {
        this.questions = questions;
        this.rule = rule;
        
        // 根据题目数量和规则获取适合的配置
        this.config = AutomaticPaperConfig.getConfig(rule.getTotalNumber(), questions.size());
        
        log.info("初始化组卷配置: 种群大小={}, 最大迭代次数={}, 交叉率={}, 变异率={}, 目标适应度={}",
                config.getPopulationSize(), config.getMaxGeneration(), 
                config.getCrossoverRate(), config.getMutationRate(), config.getTargetExpand());
    }

    /**
     * 生成试卷
     * 使用遗传算法生成满足规则要求的试卷
     * 
     * @return 生成的试卷
     */
    public Paper generatePaper() {
        // 检查题库是否为空
        if (questions == null || questions.isEmpty()) {
            log.warn("题库为空，返回空试卷");
            Paper emptyPaper = new Paper();
            emptyPaper.setAdaptationDegree(0);
            emptyPaper.setDifficulty(0);
            emptyPaper.setTagCoverage(rule.getTags());
            return emptyPaper;
        }
        
        log.info("开始组卷，题库中题目数量: {}, 需要选择题目数量: {}", questions.size(), rule.getTotalNumber());
        
        // 校验并过滤题型
        validateQuestionTypes();
        
        try {
            // 创建初始种群
            log.info("开始创建初始种群");
            Population population = new Population(config.getPopulationSize(), questions, rule);
            log.info("初始种群创建完成，种群大小: {}", population.getLength());
            
            // 记录迭代状态
            int iteration = 0;
            Paper bestPaper;
            double bestAdaptation = -1.0;
            int noImproveCount = 0; // 连续未改进次数
            
            // 迭代进化
            log.info("开始遗传算法迭代过程");
            do {
                iteration++;
                log.info("第 {} 次迭代开始", iteration);
                
                // 记录迭代开始时间
                long startTime = System.currentTimeMillis();
                
                // 进化种群
                population = evolvePopulation(population);
                bestPaper = population.getFitness();
                
                // 计算迭代耗时
                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;
                
                // 检查是否有改进
                boolean improved = false;
                if (bestPaper.getAdaptationDegree() > bestAdaptation) {
                    improved = true;
                    bestAdaptation = bestPaper.getAdaptationDegree();
                    noImproveCount = 0;
                } else {
                    noImproveCount++;
                }
                
                // 记录迭代结果
                log.info("第 {} 次迭代完成，耗时: {}ms，当前最佳适应度: {}, 题目数量: {}, 难度系数: {}, 是否有改进: {}", 
                        iteration, timeElapsed, bestPaper.getAdaptationDegree(), 
                        bestPaper.getQuestionSize(), bestPaper.getDifficulty(), improved);
                
                // 定期记录详细信息
                if (iteration % 5 == 0 || improved) {
                    log.info("第 {} 次迭代，最佳试卷知识点覆盖率: {}", iteration, bestPaper.getTagCoverage());
                }
                
                // 提前终止条件：连续多次未改进且已达到最小迭代次数
                if (noImproveCount >= config.getMaxNoImproveCount() && iteration >= config.getMinGeneration()) {
                    log.info("连续{}次迭代没有改进，已达到最小迭代次数{}，提前终止迭代", 
                            noImproveCount, config.getMinGeneration());
                    break;
                }
            } while (iteration < config.getMaxGeneration() && bestPaper.getAdaptationDegree() < config.getTargetExpand());
            
            log.info("遗传算法迭代结束，共迭代 {} 次，最终最佳适应度: {}", iteration, bestPaper.getAdaptationDegree());
            return population.getFitness();
        } catch (Exception e) {
            log.error("遗传算法执行异常，回退到随机选题: {}", e.getMessage(), e);
            
            // 异常情况下采用随机选题
            return createRandomPaper();
        }
    }

    /**
     * 校验可用题型
     */
    private void validateQuestionTypes() {
        if (rule.getQuestionType() != null && !rule.getQuestionType().isEmpty()) {
            // 获取题库中所有题型
            Set<QuestionTypeEnum> availableTypes = questions.stream()
                    .map(Question::getType)
                    .collect(Collectors.toSet());
                    
            // 过滤出题库中存在的题型
            List<QuestionTypeEnum> validTypes = rule.getQuestionType().stream()
                    .filter(availableTypes::contains)
                    .collect(Collectors.toList());
                    
            // 如果没有有效题型，使用题库中的题型
            if (validTypes.isEmpty() && !availableTypes.isEmpty()) {
                log.info("未找到规则中指定的有效题型，使用题库中所有题型: {}", availableTypes);
                rule.setQuestionType(new ArrayList<>(availableTypes));
            } else if (!validTypes.isEmpty()) {
                log.info("使用题库中存在的有效题型: {}", validTypes);
                rule.setQuestionType(validTypes);
            }
        }
    }
    
    /**
     * 创建随机试卷（用于异常处理时的回退方案）
     */
    private Paper createRandomPaper() {
        Paper fallbackPaper = new Paper();
        int limit = Math.min(rule.getTotalNumber(), questions.size());
        
        // 随机选择题目
        List<Question> shuffledQuestions = new ArrayList<>(questions);
        Collections.shuffle(shuffledQuestions);
        
        for (int i = 0; i < limit; i++) {
            fallbackPaper.addQuestion(shuffledQuestions.get(i));
        }
        
        fallbackPaper.setTagCoverage(rule.getTags());
        fallbackPaper.setAdaptationDegree(rule, AutomaticPaperConfig.TAG_WEIGHT, AutomaticPaperConfig.DIFFICULTY_WEIGHT);
        
        log.info("随机选题完成，生成试卷题目数量: {}, 适应度: {}", fallbackPaper.getQuestionSize(), fallbackPaper.getAdaptationDegree());
        return fallbackPaper;
    }

    /**
     * 种群进化
     * 通过选择、交叉和变异操作生成新一代种群
     * 
     * @param currentPopulation 当前种群
     * @return 进化后的新种群
     */
    private Population evolvePopulation(Population currentPopulation) {
        // 创建新一代种群
        Population nextPopulation = new Population(config.getPopulationSize());
        int newPopulationIndex = 0;
        
        // 精英保留策略
        if (config.isElitism()) {
            Paper elitePaper = currentPopulation.getFitness();
            nextPopulation.addPaper(elitePaper);
            newPopulationIndex++;
            log.debug("保留精英个体，适应度: {}", elitePaper.getAdaptationDegree());
        }
        
        // 交叉操作
        log.debug("开始种群交叉操作");
        while (newPopulationIndex < config.getPopulationSize()) {
            // 选择父代
            Paper parent1 = selectParent(currentPopulation);
            Paper parent2 = selectParent(currentPopulation);
            
            // 确保选择不同的父代
            int maxAttempts = 5;
            int attempts = 0;
            while (parent2.getId() == parent1.getId() && attempts < maxAttempts) {
                parent2 = selectParent(currentPopulation);
                attempts++;
            }
            
            // 交叉产生子代
            Paper child = crossover(parent1, parent2);
            
            // 变异
            mutate(child);
            
            // 计算适应度
            child.setTagCoverage(rule.getTags());
            child.setAdaptationDegree(rule, AutomaticPaperConfig.TAG_WEIGHT, AutomaticPaperConfig.DIFFICULTY_WEIGHT);
            
            // 添加到新种群
            nextPopulation.addPaper(child);
            newPopulationIndex++;
            
            log.trace("生成新个体：父代1(ID={}, 适应度={}), 父代2(ID={}, 适应度={}), 子代适应度={}", 
                    parent1.getId(), parent1.getAdaptationDegree(), 
                    parent2.getId(), parent2.getAdaptationDegree(),
                    child.getAdaptationDegree());
        }
        
        // 更新新种群统计信息
        nextPopulation.updatePopulationStatistics();
        
        log.debug("种群进化完成，新种群大小: {}, 平均适应度: {}, 最佳适应度: {}", 
                nextPopulation.getLength(), nextPopulation.getAvgAdaptation(), nextPopulation.getMaxAdaptation());
        
        return nextPopulation;
    }

    /**
     * 选择操作 - 锦标赛选择法
     * 从种群中随机选择一组个体，返回其中适应度最高的个体
     * 
     * @param population 当前种群
     * @return 选中的个体
     */
    private Paper selectParent(Population population) {
        // 锦标赛选择
        int tournamentSize = Math.min(config.getTournamentSize(), population.getLength());
        
        // 处理极端情况
        if (tournamentSize < 1) {
            log.warn("种群大小不足以进行锦标赛选择，直接返回随机个体");
            int randomIndex = random.nextInt(population.getLength());
            return population.getPaper(randomIndex);
        }
        
        // 随机选择一组候选个体
        Population candidates = population.getChildPopulation(tournamentSize);
        
        // 返回候选组中适应度最高的个体
        Paper selected = candidates.getFitness();
        log.trace("选择操作: 从{}个候选者中选择适应度为{}的个体", tournamentSize, selected.getAdaptationDegree());
        
        return selected;
    }

    /**
     * 交叉操作
     * 将两个父代个体的基因进行重组，产生新的子代
     * 
     * @param parent1 父代1
     * @param parent2 父代2
     * @return 子代个体
     */
    private Paper crossover(Paper parent1, Paper parent2) {
        Paper child = new Paper();
        
        // 设置随机ID
        child.setId(parent1.getId() * 1000 + parent2.getId());
        
        // 计算从第一个父代继承的题目数量
        int inheritFromParent1 = (int) (config.getCrossoverRate() * parent1.getQuestionSize());
        
        // 继承父代1的部分题目
        for (int i = 0; i < inheritFromParent1 && i < parent1.getQuestionSize(); i++) {
            Question question = parent1.getQuestion(i);
            if (question != null) {
                child.addQuestion(question);
            }
        }
        
        // 从父代2继承剩余题目，避免重复
        for (int i = 0; i < parent2.getQuestionSize() && child.getQuestionSize() < rule.getTotalNumber(); i++) {
            Question question = parent2.getQuestion(i);
            
            // 检查子代是否已包含该题目
            if (question != null && !child.containsQuestion(question)) {
                child.addQuestion(question);
            }
        }
        
        // 如果子代题目数量不足，从题库中随机添加
        if (child.getQuestionSize() < rule.getTotalNumber()) {
            fillWithRandomQuestions(child, rule.getTotalNumber() - child.getQuestionSize());
        }
        
        log.trace("交叉操作完成: 父代1题目数={}, 父代2题目数={}, 子代题目数={}",
                parent1.getQuestionSize(), parent2.getQuestionSize(), child.getQuestionSize());
        
        return child;
    }

    /**
     * 变异操作
     * 以一定概率替换试卷中的题目，增加多样性
     * 
     * @param paper 待变异的试卷
     */
    private void mutate(Paper paper) {
        int mutatedCount = 0;
        
        // 遍历试卷中的每个题目，以变异率概率进行变异
        for (int i = 0; i < paper.getQuestionSize(); i++) {
            if (random.nextDouble() < config.getMutationRate()) {
                Question oldQuestion = paper.getQuestion(i);
                
                // 获取一个不在试卷中的随机题目
                Question newQuestion = getRandomQuestionNotIn(paper);
                
                // 如果找到了合适的题目，进行替换
                if (newQuestion != null) {
                    paper.saveQuestion(i, newQuestion);
                    mutatedCount++;
                    
                    log.trace("变异操作: 位置={}, 旧题目ID={}, 新题目ID={}", 
                            i, oldQuestion.getId(), newQuestion.getId());
                }
            }
        }
        
        log.debug("变异操作完成，共变异{}道题目", mutatedCount);
    }
    
    /**
     * 获取一个不在试卷中的随机题目
     * 
     * @param paper 试卷
     * @return 随机题目
     */
    private Question getRandomQuestionNotIn(Paper paper) {
        // 最大尝试次数，避免无限循环
        int maxAttempts = Math.min(questions.size(), 50);
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int randomIndex = random.nextInt(questions.size());
            Question candidate = questions.get(randomIndex);
            
            if (!paper.containsQuestion(candidate)) {
                return candidate;
            }
        }
        
        // 如果多次尝试仍未找到，返回null
        log.trace("无法找到不在试卷中的随机题目，尝试次数: {}", maxAttempts);
        return null;
    }
    
    /**
     * 从题库中随机选择题目填充试卷
     * 
     * @param paper 待填充的试卷
     * @param count 需要添加的题目数量
     */
    private void fillWithRandomQuestions(Paper paper, int count) {
        if (count <= 0) return;
        
        log.trace("需要从题库中随机添加{}道题目到试卷", count);
        int added = 0;
        int attempts = 0;
        int maxAttempts = questions.size() * 3;
        
        while (added < count && attempts < maxAttempts) {
            attempts++;
            Question randomQuestion = getRandomQuestion();
            
            if (!paper.containsQuestion(randomQuestion)) {
                paper.addQuestion(randomQuestion);
                added++;
                log.trace("填充题目: 添加题目ID={}, 当前已添加{}/{}题", randomQuestion.getId(), added, count);
            }
        }
        
        if (added < count) {
            log.warn("无法添加足够的题目，已添加{}/{}题，尝试次数：{}", added, count, attempts);
        }
    }
    
    /**
     * 获取随机题目
     * 
     * @return 随机题目
     */
    private Question getRandomQuestion() {
        int randomIndex = random.nextInt(questions.size());
        return questions.get(randomIndex);
    }
}
