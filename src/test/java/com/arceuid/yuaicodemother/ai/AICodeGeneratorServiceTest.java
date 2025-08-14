package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.ai.model.HtmlCodeResult;
import com.arceuid.yuaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AICodeGeneratorServiceTest {
    @Resource
    private AICodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("请生成一个简单的HTML页面,30行代码之内");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("请生成一个简单的多文件HTML页面,50行代码之内");
        Assertions.assertNotNull(result);
    }
}