package com.baymax.exam.center.autopaper;

import com.baymax.exam.center.enums.QuestionTypeEnum;
import com.baymax.exam.center.model.Question;
import com.baymax.exam.center.vo.AutomaticPaperRuleVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author ：Baymax
 * @date ：Created in 2023/3/11 10:14
 * @description：种群 - 优化版
 * @modified By：
 * @version: 2.0
 */
@Slf4j
@Data
public class Population {
    // 种群中的个体（试卷）数组
    private Paper[] papers;
    
    // 当前种群中的个体数量
    private int paperIndex = 0;
    
    // 种群统计信息
    private double avgAdaptation = 0.0;
    private double maxAdaptation = 0.0;
    private double minAdaptation = 0.0;
    
    /**
     * 创建初始种群
     * 
     * @param populationSize 种群大小
     * @param questionList 候选题目列表
     * @param rule 组卷规则
     */
    public Population(int populationSize, List<Question> questionList, AutomaticPaperRuleVo rule) {
        log.debug("开始创建种群，种群大小：{}，题库大小：{}", populationSize, questionList.size());
        
        // 打乱题目顺序，保证随机性
        List<Question> shuffledQuestions = new ArrayList<>(questionList);
        Collections.shuffle(shuffledQuestions);
        
        // 按题型分组
        Map<QuestionTypeEnum, List<Question>> questionGroups = shuffledQuestions.stream()
                .collect(Collectors.groupingBy(Question::getType));
        
        log.debug("题库分组完成，共有{}种题型", questionGroups.size());
        for (Map.Entry<QuestionTypeEnum, List<Question>> entry : questionGroups.entrySet()) {
            log.debug("题型: {}, 数量: {}", entry.getKey(), entry.getValue().size());
        }
        
        papers = new Paper[populationSize];
        
        // 创建初始种群中的每个个体
        for (int i = 0; i < populationSize; i++) {
            log.debug("创建第{}个试卷", i+1);
            Paper paper = new Paper();
            paper.setId(i + 1);
            paper.getQuestionList().clear();
            
            // 获取题目
            List<QuestionTypeEnum> questionType = rule.getQuestionType();
            
            // 判断是否需要按题型分配题目
            if (questionType != null && !questionType.isEmpty()) {
                createPaperByQuestionType(paper, questionGroups, rule);
            } else {
                createRandomPaper(paper, shuffledQuestions, rule);
            }
            
            // 计算试卷知识点覆盖率和适应度
            paper.setTagCoverage(rule.getTags());
            paper.setAdaptationDegree(rule, AutomaticPaperConfig.TAG_WEIGHT, AutomaticPaperConfig.DIFFICULTY_WEIGHT);
            
            papers[i] = paper;
            log.debug("第{}个试卷创建完成，适应度：{}, 知识点覆盖率：{}, 难度：{}", 
                    i+1, paper.getAdaptationDegree(), paper.getTagCoverage(), paper.getDifficulty());
        }
        
        // 计算初始种群的适应度统计
        updatePopulationStatistics();
        
        log.info("初始种群创建完成，种群大小：{}，平均适应度：{}，最大适应度：{}，最小适应度：{}", 
                papers.length, avgAdaptation, maxAdaptation, minAdaptation);
    }
    
    /**
     * 根据题型创建试卷
     */
    private void createPaperByQuestionType(Paper paper, Map<QuestionTypeEnum, List<Question>> questionGroups, AutomaticPaperRuleVo rule) {
        log.debug("按题型规则组卷，题型数量：{}", rule.getQuestionType().size());
        
        // 获取题型百分比，如果未指定则自动计算
        Map<QuestionTypeEnum, Float> percentage = new HashMap<>();
        if (rule.getPercentage() == null || rule.getPercentage().isEmpty()) {
            log.debug("规则中未指定题型百分比，使用题库中实际占比");
            
            // 计算题库中各题型占比
            int totalQuestions = 0;
            for (List<Question> questions : questionGroups.values()) {
                totalQuestions += questions.size();
            }
            
            for (Map.Entry<QuestionTypeEnum, List<Question>> entry : questionGroups.entrySet()) {
                percentage.put(entry.getKey(), (float) entry.getValue().size() / totalQuestions);
            }
        } else {
            percentage = new HashMap<>(rule.getPercentage());
            log.debug("使用规则中指定的题型百分比：{}", percentage);
        }
        
        // 计算每种题型应选择的题目数量
        int remainingQuestions = rule.getTotalNumber();
        Map<QuestionTypeEnum, Integer> assignmentNumber = new HashMap<>();
        Set<QuestionTypeEnum> availableTypes = new HashSet<>();
        
        // 第一轮分配 - 根据百分比初步分配
        for (Map.Entry<QuestionTypeEnum, Float> entry : percentage.entrySet()) {
            QuestionTypeEnum type = entry.getKey();
            float percent = entry.getValue();
            
            // 检查题型是否在题库中存在
            if (!questionGroups.containsKey(type) || questionGroups.get(type).isEmpty()) {
                assignmentNumber.put(type, 0);
                log.debug("题型{}在题库中不存在或为空，设置为0题", type);
                continue;
            }
            
            // 计算应分配的题目数
            int expectedCount = Math.round(percent * rule.getTotalNumber());
            int availableCount = questionGroups.get(type).size();
            
            // 取可用题目数和期望题目数的较小值
            int assignedCount = Math.min(expectedCount, availableCount);
            assignmentNumber.put(type, assignedCount);
            remainingQuestions -= assignedCount;
            
            // 如果该题型还有剩余可用题目，加入可用类型集合
            if (assignedCount < availableCount) {
                availableTypes.add(type);
            }
            
            log.debug("题型{}初始分配{}题（期望{}题，可用{}题）", type, assignedCount, expectedCount, availableCount);
        }
        
        // 第二轮分配 - 分配剩余题目
        while (remainingQuestions > 0 && !availableTypes.isEmpty()) {
            // 将剩余题目平均分配给剩余可用题型
            int typeCount = availableTypes.size();
            int baseAddition = remainingQuestions / typeCount;
            int extra = remainingQuestions % typeCount;
            
            Iterator<QuestionTypeEnum> iterator = availableTypes.iterator();
            while (iterator.hasNext() && remainingQuestions > 0) {
                QuestionTypeEnum type = iterator.next();
                int currentAssigned = assignmentNumber.getOrDefault(type, 0);
                int availableCount = questionGroups.get(type).size();
                
                // 计算本轮可以分配的额外题目数
                int addition = baseAddition + (extra > 0 ? 1 : 0);
                if (extra > 0) extra--;
                
                // 确保不超过可用题目数
                int newAssigned = Math.min(currentAssigned + addition, availableCount);
                int actualAdded = newAssigned - currentAssigned;
                
                assignmentNumber.put(type, newAssigned);
                remainingQuestions -= actualAdded;
                
                // 如果该题型已分配满，从可用类型中移除
                if (newAssigned >= availableCount) {
                    iterator.remove();
                }
                
                log.debug("题型{}第二轮分配额外{}题，当前总计{}题", type, actualAdded, newAssigned);
            }
            
            // 如果无法继续分配，退出循环
            if (baseAddition == 0 && extra == 0) {
                break;
            }
        }
        
        log.debug("最终题型分配方案：{}，剩余未分配题目：{}", assignmentNumber, remainingQuestions);
        
        // 根据分配方案选择题目
        for (Map.Entry<QuestionTypeEnum, Integer> entry : assignmentNumber.entrySet()) {
            QuestionTypeEnum type = entry.getKey();
            int count = entry.getValue();
            
            if (count <= 0 || !questionGroups.containsKey(type)) {
                continue;
            }
            
            List<Question> typeQuestions = new ArrayList<>(questionGroups.get(type));
            Collections.shuffle(typeQuestions); // 随机打乱顺序
            
            // 选择指定数量的题目
            List<Question> selectedQuestions = typeQuestions.stream()
                    .limit(count)
                    .collect(Collectors.toList());
            
            paper.getQuestionList().addAll(selectedQuestions);
            log.debug("题型{}添加{}道题目", type, selectedQuestions.size());
        }
    }
    
    /**
     * 随机创建试卷
     */
    private void createRandomPaper(Paper paper, List<Question> questionList, AutomaticPaperRuleVo rule) {
        log.debug("未指定题型，随机选择{}道题目", rule.getTotalNumber());
        
        // 随机选择题目
        int totalToSelect = Math.min(rule.getTotalNumber(), questionList.size());
        Set<Integer> selectedIndices = new HashSet<>();
        
        for (int i = 0; i < totalToSelect; i++) {
            // 生成一个随机索引，确保不重复选择
            int randomIndex;
            do {
                randomIndex = ThreadLocalRandom.current().nextInt(questionList.size());
            } while (selectedIndices.contains(randomIndex));
            
            selectedIndices.add(randomIndex);
            paper.addQuestion(questionList.get(randomIndex));
        }
    }
    
    /**
     * 创建指定大小的空种群
     */
    public Population(int populationSize) {
        papers = new Paper[populationSize];
        log.debug("创建空种群，大小：{}", populationSize);
    }
    
    /**
     * 从已有试卷数组创建种群
     */
    public Population(Paper[] paperList) {
        papers = paperList;
        paperIndex = paperList.length;
        updatePopulationStatistics();
        log.debug("从Paper数组创建种群，大小：{}", paperList.length);
    }
    
    /**
     * 获取种群中适应度最高的个体
     */
    public Paper getFitness() {
        Paper bestPaper = Arrays.stream(papers)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(Paper::getAdaptationDegree))
                .orElse(new Paper());
        
        log.debug("获取种群最佳个体，ID：{}，适应度：{}", bestPaper.getId(), bestPaper.getAdaptationDegree());
        return bestPaper;
    }
    
    /**
     * 获取随机子种群
     * 
     * @param number 子种群大小
     * @return 子种群
     */
    public Population getChildPopulation(int number) {
        log.trace("获取子种群，大小：{}", number);
        
        // 确保请求的子种群大小不超过当前种群大小
        int actualSize = Math.min(number, getLength());
        
        if (actualSize <= 0) {
            log.warn("请求的子种群大小不合法：{}，返回空种群", number);
            return new Population(new Paper[0]);
        }
        
        // 随机选择个体
        List<Paper> validPapers = Arrays.stream(papers)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        Collections.shuffle(validPapers);
        
        List<Paper> selectedPapers = validPapers.stream()
                .limit(actualSize)
                .collect(Collectors.toList());
        
        return new Population(selectedPapers.toArray(new Paper[0]));
    }
    
    /**
     * 获取种群中指定索引的个体
     */
    public Paper getPaper(int index) {
        if (index < 0 || index >= papers.length || papers[index] == null) {
            log.warn("请求的个体索引无效：{}", index);
            return null;
        }
        return papers[index];
    }
    
    /**
     * 将个体添加到种群中
     */
    public void addPaper(Paper paper) {
        if (paperIndex < papers.length) {
            papers[paperIndex++] = paper;
            log.trace("添加试卷到种群，ID：{}，当前种群大小：{}/{}", 
                    paper.getId(), paperIndex, papers.length);
        } else {
            log.warn("种群已满，无法添加更多试卷，当前大小：{}", papers.length);
        }
    }
    
    /**
     * 返回种群大小
     */
    public int getLength() {
        return (int) Arrays.stream(papers).filter(Objects::nonNull).count();
    }
    
    /**
     * 更新种群统计信息
     */
    public void updatePopulationStatistics() {
        // 筛选出有效的个体
        List<Paper> validPapers = Arrays.stream(papers)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (validPapers.isEmpty()) {
            avgAdaptation = 0;
            maxAdaptation = 0;
            minAdaptation = 0;
            return;
        }
        
        // 计算平均适应度
        avgAdaptation = validPapers.stream()
                .mapToDouble(Paper::getAdaptationDegree)
                .average()
                .orElse(0);
        
        // 计算最大适应度
        maxAdaptation = validPapers.stream()
                .mapToDouble(Paper::getAdaptationDegree)
                .max()
                .orElse(0);
        
        // 计算最小适应度
        minAdaptation = validPapers.stream()
                .mapToDouble(Paper::getAdaptationDegree)
                .min()
                .orElse(0);
        
        log.debug("种群统计信息更新：平均适应度={}，最大适应度={}，最小适应度={}", 
                avgAdaptation, maxAdaptation, minAdaptation);
    }
}
