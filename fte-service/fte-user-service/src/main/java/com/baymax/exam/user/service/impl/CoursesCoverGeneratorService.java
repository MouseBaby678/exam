package com.baymax.exam.user.service.impl;

import com.baymax.exam.common.core.exception.ResultException;
import com.baymax.exam.common.core.result.Result;
import com.baymax.exam.file.feign.FileDetailClient;
import com.baymax.exam.web.utils.UserAuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * 生成课程封面的服务
 */
@Slf4j
@Service
public class CoursesCoverGeneratorService {
    
    @Autowired
    private FileDetailClient fileDetailClient;
    
    private final Random random = new Random();
    
    // 封面配置
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    
    // 背景颜色列表
    private final Color[] backgroundColors = {
            new Color(52, 152, 219),  // 蓝色
            new Color(46, 204, 113),  // 绿色
            new Color(231, 76, 60),   // 红色
            new Color(155, 89, 182),  // 紫色
            new Color(241, 196, 15),  // 黄色
            new Color(52, 73, 94),    // 深蓝色
            new Color(230, 126, 34)   // 橙色
    };
    
    // 生成课程封面
    public String generateCourseCover(String courseName) throws ResultException {
        try {
            // 创建图像
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // 启用抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // 设置随机背景色
            Color backgroundColor = backgroundColors[random.nextInt(backgroundColors.length)];
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            
            // 绘制一些随机装饰图形
            drawDecoration(g2d);
            
            // 绘制课程名称
            drawCourseName(g2d, courseName);
            
            // 释放资源
            g2d.dispose();
            
            // 将BufferedImage转换为MultipartFile
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            
            // 创建临时文件
            MultipartFile multipartFile = new InMemoryMultipartFile("file", "cover.png", "image/png", bytes);
            
            // 上传到文件服务
            Integer userId = UserAuthUtil.getUserId();
            Result<String> result = fileDetailClient.uploadImage(multipartFile, "/"+userId+"/course/", null, "course");
            return result.getResultDate();
            
        } catch (IOException e) {
            log.error("生成课程封面出错", e);
            throw new ResultException("生成课程封面失败");
        }
    }
    
    // 绘制装饰
    private void drawDecoration(Graphics2D g2d) {
        // 绘制一些半透明的图形
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        
        // 绘制一些随机圆形
        for (int i = 0; i < 10; i++) {
            int size = 30 + random.nextInt(150);
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            
            g2d.setColor(new Color(255, 255, 255, 90 + random.nextInt(100)));
            g2d.fillOval(x, y, size, size);
        }
        
        // 重置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    // 绘制课程名称
    private void drawCourseName(Graphics2D g2d, String courseName) {
        g2d.setColor(Color.WHITE);
        
        // 计算合适的字体大小，确保文字不会超出图片
        int fontSize = 60;
        Font font = new Font("微软雅黑", Font.BOLD, fontSize);
        g2d.setFont(font);
        
        // 如果文字太长，减小字体大小
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(courseName);
        
        while (textWidth > WIDTH - 80 && fontSize > 20) {
            fontSize -= 5;
            font = new Font("微软雅黑", Font.BOLD, fontSize);
            g2d.setFont(font);
            fontMetrics = g2d.getFontMetrics();
            textWidth = fontMetrics.stringWidth(courseName);
        }
        
        // 绘制文字阴影效果
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(courseName, (WIDTH - textWidth) / 2 + 3, HEIGHT / 2 + 3);
        
        // 绘制文字
        g2d.setColor(Color.WHITE);
        g2d.drawString(courseName, (WIDTH - textWidth) / 2, HEIGHT / 2);
    }
    
    // 内存中的MultipartFile实现
    private static class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;
        
        public InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }
        
        @Override
        public long getSize() {
            return content.length;
        }
        
        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }
        
        @Override
        public ByteArrayInputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }
        
        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
} 