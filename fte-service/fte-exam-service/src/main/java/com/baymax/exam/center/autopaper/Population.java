package com.baymax.exam.center.autopaper;

import com.baymax.exam.center.enums.QuestionTypeEnum;
import com.baymax.exam.center.model.Question;
import com.baymax.exam.center.vo.AutomaticPaperRuleVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author ：Baymax
 * @date ：Created in 2023/3/11 10:14
 * @description：种群
 * @modified By：
 * @version:
 */
@Slf4j
@Data
public class Population {

    Paper[] papers;
    private int paperIndex=0;
    public Population(int populationSize, List<Question> questionList, AutomaticPaperRuleVo rule) {
        log.debug("开始创建种群，种群大小：{}，题库大小：{}", populationSize, questionList.size());
        Collections.shuffle(questionList);
        Map<QuestionTypeEnum,List<Question>> questionGroups=questionList.stream().collect(Collectors.groupingBy(Question::getType));
        log.debug("题库分组完成，共有{}种题型", questionGroups.size());
        for (Map.Entry<QuestionTypeEnum, List<Question>> entry : questionGroups.entrySet()) {
            log.debug("题型: {}, 数量: {}", entry.getKey(), entry.getValue().size());
        }
        
        papers = new Paper[populationSize];
        Paper paper;
        for (int i = 0; i < populationSize; i++) {
            log.debug("创建第{}个试卷", i+1);
            paper = new Paper();
            paper.setId(i + 1);
            //去除总分限制，
//           while (paper.getTotalScore()>=rule.getTotalScore()){
            paper.getQuestionList().clear();
            //获取题目
            Paper finalPaper = paper;
            List<QuestionTypeEnum> questionType = rule.getQuestionType();
            //此组卷方式有很大几率失败，如果要对占比重新分配
            if(questionType!=null&&!questionType.isEmpty()){
                log.debug("按题型规则组卷，题型数量：{}", questionType.size());
                Map<QuestionTypeEnum, Float> percentage = new HashMap<>(rule.getPercentage());
                //没分配就按题库题型实际占比
                if(rule.getPercentage()==null||rule.getPercentage().isEmpty()){
                    log.debug("规则中未指定题型百分比，使用题库中实际占比");
                    for (QuestionTypeEnum key:questionGroups.keySet()) {
                        percentage.put(key,(float)questionGroups.get(key).size()/questionList.size());
                    }
                } else {
                    log.debug("使用规则中指定的题型百分比：{}", percentage);
                }
                //预分配题型
                int surplusNumber=rule.getTotalNumber();
                List<Question> list=new ArrayList<>();
                Set<QuestionTypeEnum> surplusKeys = new HashSet<>();
                Map<QuestionTypeEnum,Integer> assignmentNumber=new HashMap<>();
                
                // 先过滤掉题库中不存在的题型
                for (QuestionTypeEnum key : percentage.keySet()) {
                    if (questionGroups.containsKey(key) && questionGroups.get(key) != null && !questionGroups.get(key).isEmpty()) {
                        surplusKeys.add(key);
                    } else {
                        // 将不存在的题型设置为0题
                        assignmentNumber.put(key, 0);
                        log.debug("题型{}在题库中不存在或为空，设置为0题", key);
                    }
                }
                
                // 如果过滤后没有可用题型，随机选择可用题型避免异常
                if (surplusKeys.isEmpty() && !questionGroups.isEmpty()) {
                    QuestionTypeEnum randomType = questionGroups.keySet().iterator().next();
                    surplusKeys.add(randomType);
                    percentage.put(randomType, 1.0f);
                    log.debug("没有有效题型，随机选择题型{}", randomType);
                }
                
                for (QuestionTypeEnum key : surplusKeys) {
                    float percent = percentage.getOrDefault(key, 0f);
                    //预期题数
                    int expectNumber = (int) (percent * rule.getTotalNumber());
                    //试卷题数
                    int actualNumber = questionGroups.get(key).size();
                    //不够分配
                    if(expectNumber > actualNumber){
                        assignmentNumber.put(key, actualNumber);
                        surplusNumber -= actualNumber;
                        log.debug("题型{}预期题数{}大于实际可用题数{}，使用全部可用题", key, expectNumber, actualNumber);
                    } else {
                        assignmentNumber.put(key, expectNumber);
                        surplusNumber -= expectNumber;
                        log.debug("题型{}预期题数{}，可用题数{}，分配{}题", key, expectNumber, actualNumber, expectNumber);
                    }
                }
                
                log.debug("初次分配后，剩余需分配题数：{}", surplusNumber);
                
                //对充足的题型，进行平均分配
                Iterator<QuestionTypeEnum> iterator;
                QuestionTypeEnum key;
                int actualNumber, realNumber;
                
                // 确保有可分配的题型再继续分配
                if (!surplusKeys.isEmpty()) {
                    while (surplusNumber > 0) {
                        iterator = new HashSet<>(surplusKeys).iterator();
                        boolean anyAssigned = false;
                        
                        while (iterator.hasNext()) {
                            key = iterator.next();
                            actualNumber = questionGroups.get(key).size();
                            realNumber = assignmentNumber.getOrDefault(key, 0);
                            
                            if (realNumber < actualNumber) {
                                surplusNumber--;
                                assignmentNumber.put(key, ++realNumber);
                                anyAssigned = true;
                            } else {
                                surplusKeys.remove(key);
                                log.trace("题型{}已分配满，从剩余可分配题型中移除", key);
                            }
                            
                            if (surplusNumber == 0) {
                                break;
                            }
                        }
                        
                        // 如果无法继续分配，退出循环避免死循环
                        if (!anyAssigned || surplusKeys.isEmpty()) {
                            if (surplusNumber > 0) {
                                log.debug("无法继续分配剩余{}题，退出分配循环", surplusNumber);
                            }
                            break;
                        }
                    }
                }
                
                log.debug("最终分配方案：{}", assignmentNumber);
                
                assignmentNumber.forEach((type, number) -> {
                    if (number > 0 && questionGroups.containsKey(type)) {
                        List<Question> typeQuestions = questionGroups.get(type);
                        if (typeQuestions != null && !typeQuestions.isEmpty()) {
                            List<Question> selectedQuestions = typeQuestions.stream().limit(number).toList();
                            finalPaper.getQuestionList().addAll(selectedQuestions);
                            log.trace("题型{}添加{}道题目", type, selectedQuestions.size());
                        }
                    }
                });
            } else {
                log.debug("未指定题型，随机选择{}道题目", rule.getTotalNumber());
                for (int j=0;j<rule.getTotalNumber();j++){
                    finalPaper.addQuestion(questionList.get(j));
                }
            }
            // 计算试卷知识点覆盖率
            finalPaper.setTagCoverage(rule.getTags());
            // 计算试卷适应度
            finalPaper.setAdaptationDegree(rule, AutomaticPaperConfig.TAG_WEIGHT,AutomaticPaperConfig.DIFFICULTY_WEIGHT);
            papers[i] = finalPaper;
            log.debug("第{}个试卷创建完成，适应度：{}, 知识点覆盖率：{}, 难度：{}", 
                    i+1, finalPaper.getAdaptationDegree(), finalPaper.getTagCoverage(), finalPaper.getDifficulty());
        }
        
        // 计算初始种群的适应度统计
        double avgAdaptation = Arrays.stream(papers).mapToDouble(Paper::getAdaptationDegree).average().orElse(0);
        double maxAdaptation = Arrays.stream(papers).mapToDouble(Paper::getAdaptationDegree).max().orElse(0);
        double minAdaptation = Arrays.stream(papers).mapToDouble(Paper::getAdaptationDegree).min().orElse(0);
        log.info("初始种群创建完成，种群大小：{}，平均适应度：{}，最大适应度：{}，最小适应度：{}", 
                papers.length, avgAdaptation, maxAdaptation, minAdaptation);
    }

    public Population(int populationSize) {
        papers = new Paper[populationSize];
        log.debug("创建空种群，大小：{}", populationSize);
    }
    
    public Population(Paper[] list) {
        papers = list;
        paperIndex=list.length-1;
        log.debug("从Paper数组创建种群，大小：{}", list.length);
    }
    
    /**
     * 获取种群中最优秀个体
     *
     * @return {@link Paper}
     */
    public Paper getFitness() {
        Paper bestPaper = Arrays.stream(papers).max(Comparator.comparing(Paper::getAdaptationDegree)).get();
        log.debug("获取种群最佳个体，ID：{}，适应度：{}", bestPaper.getId(), bestPaper.getAdaptationDegree());
        return bestPaper;
    }

    public Population getChildPopulation(int number){
        log.trace("获取子种群，大小：{}", number);
        //乱序
        List<Paper> list=new ArrayList<>(Arrays.asList(papers));
        Collections.shuffle(list);
        list=list.stream().limit(number).toList();
        //选number
        Paper[] childPapers= list.toArray(new Paper[number]);
        return new Population(childPapers);
    }

    /**
     * 获取种群中某个个体
     *
     * @param index
     * @return
     */
    public Paper getPaper(int index) {
        return papers[index];
    }

    /**
     * 设置种群中某个个体
     *
     * @param paper
     */
    public void addPaper(Paper paper) {
        if(paperIndex<papers.length){
            papers[paperIndex++] = paper;
            log.trace("添加试卷到种群，ID：{}，当前种群大小：{}/{}", paper.getId(), paperIndex, papers.length);
        } else {
            log.warn("种群已满，无法添加更多试卷，当前大小：{}", papers.length);
        }
    }

    /**
     * 返回种群规模
     *
     * @return
     */
    public int getLength() {
        return papers.length;
    }
}
