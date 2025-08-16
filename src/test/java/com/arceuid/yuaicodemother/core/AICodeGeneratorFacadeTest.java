package com.arceuid.yuaicodemother.core;

import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AICodeGeneratorFacadeTest {
    @Resource
    private AICodeGeneratorFacade aICodeGeneratorFacade;

    @Test
    void generateAndSave() {
        Flux<String> result = aICodeGeneratorFacade.generateAndSaveStream("写一个猫娘的简单介绍的html页面,不超过200行", CodeGenTypeEnum.MULTI_FILE, 1L);
        //阻塞直到Flux流结束
        List<String> resultCode = result.collectList().block();
        assertNotNull(resultCode);
        String resultCodeStr = String.join("", resultCode);
        assertNotNull(resultCodeStr);
    }

    @Test
    void generateAndSaveHtmlCodeStream() {
        Flux<String> result = aICodeGeneratorFacade.generateAndSaveStream("写一个简单介绍的html页面,不超过20行", CodeGenTypeEnum.HTML, 1L);
        //阻塞直到Flux流结束
        List<String> resultCode = result.collectList().block();
        assertNotNull(resultCode);
        String resultCodeStr = String.join("", resultCode);
        assertNotNull(resultCodeStr);
    }
}