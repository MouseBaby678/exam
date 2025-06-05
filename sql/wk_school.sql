/*
 Navicat Premium Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80035 (8.0.35)
 Source Host           : localhost:3306
 Source Schema         : wk_school

 Target Server Type    : MySQL
 Target Server Version : 80035 (8.0.35)
 File Encoding         : 65001

 Date: 10/05/2025 13:26:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ec_classes
-- ----------------------------
DROP TABLE IF EXISTS `ec_classes`;
CREATE TABLE `ec_classes`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '班级名称',
  `course_id` int UNSIGNED NOT NULL COMMENT '课程id',
  `teacher_id` int NOT NULL COMMENT '老师id:为了方便查找',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  INDEX `teacher_id`(`teacher_id` ASC) USING BTREE,
  CONSTRAINT `ec_classes_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `ec_courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ec_classes_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `es_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ec_courses
-- ----------------------------
DROP TABLE IF EXISTS `ec_courses`;
CREATE TABLE `ec_courses`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '课程名称',
  `cover` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '课程封面',
  `introduce` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '课程介绍',
  `is_public` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否公开:0：不公开（邀请码）,1公开（搜索）',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '课程状态:0:正常，1：结课',
  `user_id` int NOT NULL COMMENT '创建者',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `ec_courses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `es_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '课程信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ec_join_class
-- ----------------------------
DROP TABLE IF EXISTS `ec_join_class`;
CREATE TABLE `ec_join_class`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_id` int NOT NULL COMMENT '课程id',
  `student_id` int NOT NULL COMMENT '学生id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`, `class_id`, `student_id`) USING BTREE,
  INDEX `student_id`(`student_id` ASC) USING BTREE,
  INDEX `class_id`(`class_id` ASC) USING BTREE,
  CONSTRAINT `ec_join_class_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `es_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ec_join_class_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `ec_classes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 366 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for es_school
-- ----------------------------
DROP TABLE IF EXISTS `es_school`;
CREATE TABLE `es_school`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `logo` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '学校名称',
  `site` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '学校域名',
  `introduce` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '学校介绍',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '学校信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for es_school_auth
-- ----------------------------
DROP TABLE IF EXISTS `es_school_auth`;
CREATE TABLE `es_school_auth`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户id',
  `student_id` int NOT NULL COMMENT '学生id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint NULL DEFAULT 1 COMMENT '认证状态：1-有效，0-无效',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '学生认证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for es_school_department
-- ----------------------------
DROP TABLE IF EXISTS `es_school_department`;
CREATE TABLE `es_school_department`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `logo` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'logo',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '班级名称/部门',
  `parent_id` int NULL DEFAULT NULL COMMENT '上级部门',
  `leader_id` int NOT NULL COMMENT '辅导员/部门管理',
  `school_id` int NOT NULL COMMENT '学校id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `school_id`(`school_id` ASC) USING BTREE,
  CONSTRAINT `es_school_department_ibfk_2` FOREIGN KEY (`leader_id`) REFERENCES `es_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `es_school_department_ibfk_3` FOREIGN KEY (`school_id`) REFERENCES `es_school` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '学习部门信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for es_school_student
-- ----------------------------
DROP TABLE IF EXISTS `es_school_student`;
CREATE TABLE `es_school_student`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `job_no` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '工号/学号',
  `real_name` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '姓名',
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `department_id` int NOT NULL COMMENT '部门/班级',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态，1-有效，0-无效',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `department_id`(`department_id` ASC) USING BTREE,
  CONSTRAINT `es_school_student_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `es_school_department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '学校用户认证信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for es_user
-- ----------------------------
DROP TABLE IF EXISTS `es_user`;
CREATE TABLE `es_user`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '用户名',
  `nickname` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '别名',
  `password` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '密码',
  `picture` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `bg_picture` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '背景图像',
  `autograph` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '签名',
  `phone` char(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `auth_id` int NULL DEFAULT NULL COMMENT '认证id',
  `role` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '0' COMMENT '0：学生 1：教师',
  `enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态，0：启用，>0:封禁',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`, `username`) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `id`(`id` ASC) USING BTREE,
  INDEX `auth_id`(`auth_id` ASC) USING BTREE,
  CONSTRAINT `es_user_ibfk_1` FOREIGN KEY (`auth_id`) REFERENCES `es_school_auth` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 121 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '用户信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- View structure for es_user_auth_info
-- ----------------------------
DROP VIEW IF EXISTS `es_user_auth_info`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `es_user_auth_info` AS select `es_user`.`id` AS `user_id`,`es_user`.`username` AS `username`,`es_user`.`nickname` AS `nickname`,`es_user`.`picture` AS `picture`,`es_school_student`.`job_no` AS `job_no`,`es_school_student`.`real_name` AS `real_name`,`es_school_student`.`email` AS `email`,`es_school_student`.`id` AS `student_id`,`es_school_student`.`phone` AS `phone`,`es_school_student`.`department_id` AS `department_id`,`es_school_department`.`logo` AS `department_logo`,`es_school_department`.`name` AS `department_name`,`es_school_department`.`parent_id` AS `parent_id`,`es_school_department`.`leader_id` AS `leader_id`,`es_school_department`.`school_id` AS `school_id`,`es_school`.`logo` AS `school_logo`,`es_school`.`name` AS `school_name`,`es_school`.`site` AS `school_site`,`es_school_auth`.`created_at` AS `auth_time` from ((((`es_user` left join `es_school_auth` on((`es_user`.`auth_id` = `es_school_auth`.`id`))) left join `es_school_student` on((`es_school_auth`.`student_id` = `es_school_student`.`id`))) left join `es_school_department` on((`es_school_student`.`department_id` = `es_school_department`.`id`))) left join `es_school` on((`es_school_department`.`school_id` = `es_school`.`id`)));

SET FOREIGN_KEY_CHECKS = 1;
