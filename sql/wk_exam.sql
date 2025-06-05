/*
 Navicat Premium Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80035 (8.0.35)
 Source Host           : localhost:3306
 Source Schema         : wk_exam

 Target Server Type    : MySQL
 Target Server Version : 80035 (8.0.35)
 File Encoding         : 65001

 Date: 10/05/2025 13:26:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ed_exam_answer_log
-- ----------------------------
DROP TABLE IF EXISTS `ed_exam_answer_log`;
CREATE TABLE `ed_exam_answer_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `exam_id` int NOT NULL COMMENT '考试id',
  `student_id` int NOT NULL COMMENT '学生id',
  `class_id` int NULL DEFAULT NULL COMMENT '班级id',
  `status` tinyint NOT NULL COMMENT '状态：0:开始、2：交卷',
  `info` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '信息',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2130829315 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '考试作答日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ed_exam_answer_result
-- ----------------------------
DROP TABLE IF EXISTS `ed_exam_answer_result`;
CREATE TABLE `ed_exam_answer_result`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '学生id',
  `exam_info_id` int NOT NULL COMMENT '考试id',
  `question_id` int UNSIGNED NOT NULL COMMENT '题目id',
  `option_id` int NOT NULL COMMENT '选项id',
  `answer` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '答案：主观题使用',
  `result_type` tinyint(1) NULL DEFAULT NULL COMMENT '结果类型：对、错、半错',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `exam_info_id`(`exam_info_id` ASC) USING BTREE,
  INDEX `queston_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `ed_exam_answer_result_ibfk_1` FOREIGN KEY (`exam_info_id`) REFERENCES `ee_exam_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ed_exam_answer_result_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `ee_exam_question` (`question_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 366 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '考试作答结果' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ed_exam_score_record
-- ----------------------------
DROP TABLE IF EXISTS `ed_exam_score_record`;
CREATE TABLE `ed_exam_score_record`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户id',
  `exam_info_id` int NOT NULL COMMENT '考试信息id',
  `class_id` int NULL DEFAULT NULL COMMENT '班级id',
  `question_id` int UNSIGNED NOT NULL COMMENT '题目id',
  `score` float NOT NULL COMMENT '得分',
  `comment` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '评语',
  `result_type` tinyint NULL DEFAULT NULL COMMENT '批阅结果',
  `review_type` tinyint NULL DEFAULT NULL COMMENT '评阅类型',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `exam_info_id`(`exam_info_id` ASC) USING BTREE,
  INDEX `question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `ed_exam_score_record_ibfk_1` FOREIGN KEY (`exam_info_id`) REFERENCES `ee_exam_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ed_exam_score_record_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `ee_exam_question` (`question_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1545 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '考试得分' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ee_exam_class
-- ----------------------------
DROP TABLE IF EXISTS `ee_exam_class`;
CREATE TABLE `ee_exam_class`  (
  `class_id` int NOT NULL COMMENT '考试班级',
  `exam_info_id` int NOT NULL COMMENT '考试信息',
  PRIMARY KEY (`class_id`, `exam_info_id`) USING BTREE,
  INDEX `ee_exam_class_ibfk_1`(`exam_info_id` ASC) USING BTREE,
  CONSTRAINT `ee_exam_class_ibfk_1` FOREIGN KEY (`exam_info_id`) REFERENCES `ee_exam_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ee_exam_info
-- ----------------------------
DROP TABLE IF EXISTS `ee_exam_info`;
CREATE TABLE `ee_exam_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '考试标题',
  `question_disorder` tinyint(1) NOT NULL DEFAULT 0 COMMENT '题目乱序',
  `option_disorder` tinyint(1) NOT NULL DEFAULT 0 COMMENT '选项乱序',
  `end_visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '批阅可见',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `is_monitor` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启行为监控',
  `is_copy_paste` tinyint(1) NULL DEFAULT 0 COMMENT '是否允许复制',
  `course_id` int NOT NULL COMMENT '课程id',
  `exam_id` int UNSIGNED NOT NULL COMMENT '试卷id',
  `teacher_id` int NOT NULL COMMENT '老师id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ee_exam_info_ibfk_1`(`exam_id` ASC) USING BTREE,
  CONSTRAINT `ee_exam_info_ibfk_1` FOREIGN KEY (`exam_id`) REFERENCES `ee_exam_paper` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '考试信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ee_exam_paper
-- ----------------------------
DROP TABLE IF EXISTS `ee_exam_paper`;
CREATE TABLE `ee_exam_paper`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '考试标题',
  `introduce` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '考试介绍',
  `course_id` int NOT NULL COMMENT '课程id',
  `teacher_id` int NOT NULL COMMENT '教师id',
  `paper_type` int NOT NULL DEFAULT 0 COMMENT '试卷类型：0:手动组卷，1：自动组卷，2：作业',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '考试试卷信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ee_exam_question
-- ----------------------------
DROP TABLE IF EXISTS `ee_exam_question`;
CREATE TABLE `ee_exam_question`  (
  `question_id` int UNSIGNED NOT NULL,
  `exam_id` int UNSIGNED NOT NULL COMMENT '试卷标题id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`, `exam_id`) USING BTREE,
  INDEX `ee_exam_question_ibfk_2`(`exam_id` ASC) USING BTREE,
  INDEX `question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `ee_exam_question_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `eq_question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ee_exam_question_ibfk_2` FOREIGN KEY (`exam_id`) REFERENCES `ee_exam_paper` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for eq_question
-- ----------------------------
DROP TABLE IF EXISTS `eq_question`;
CREATE TABLE `eq_question`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '题目名称',
  `type` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '题目类型：0：单选、1：多选、2：判断、3：填空、4：主观',
  `analysis` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '题目解析',
  `difficulty` tinyint NOT NULL DEFAULT 2 COMMENT '题目难度',
  `score` float NOT NULL DEFAULT 5 COMMENT '题目分值',
  `is_public` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '0' COMMENT '是否公开：0:不公开、1：课程公开、2：公开',
  `course_id` int UNSIGNED NOT NULL COMMENT '课程id',
  `tag_id` int UNSIGNED NULL DEFAULT NULL COMMENT '题目标签id',
  `teacher_id` int UNSIGNED NOT NULL COMMENT '教师id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `eq_question_ibfk_1`(`tag_id` ASC) USING BTREE,
  CONSTRAINT `eq_question_ibfk_1` FOREIGN KEY (`tag_id`) REFERENCES `eq_tags` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1491 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '题目信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for eq_question_item
-- ----------------------------
DROP TABLE IF EXISTS `eq_question_item`;
CREATE TABLE `eq_question_item`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '选项内容\r\n选择性题目：为选项\r\n填空题/客观题：null\r\n文件题：为文件类型\r\n代码题：为语言类型\r\n',
  `answer` text CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '选项答案：\r\n选择性题目：非null就是正确答案\r\n填空题/客观题：为正确答案\r\n文件题：为文件答案\r\n代码题：为代码执行结果',
  `question_id` int UNSIGNED NOT NULL COMMENT '题目id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `eq_question_item_ibfk_1`(`question_id` ASC) USING BTREE,
  CONSTRAINT `eq_question_item_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `eq_question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4032 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '题目选择表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for eq_tags
-- ----------------------------
DROP TABLE IF EXISTS `eq_tags`;
CREATE TABLE `eq_tags`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `tag` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '标签名称',
  `course_id` int UNSIGNED NOT NULL COMMENT '课程id',
  `parent_id` int NULL DEFAULT NULL COMMENT '父标签Id,null为顶级标签 ',
  `teacher_id` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '题目标签表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
