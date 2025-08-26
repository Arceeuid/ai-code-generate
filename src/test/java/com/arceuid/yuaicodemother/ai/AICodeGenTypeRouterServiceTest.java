package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class AICodeGenTypeRouterServiceTest {
    @Resource
    private AICodeGenTypeRouterService aiCodeGenTypeRouterService;

    @Test
    void routeCodeGenType() {

        CodeGenTypeEnum codeGenTypeEnum1 = aiCodeGenTypeRouterService.routeCodeGenType("帮我生成一个简单的个人介绍界面");
        log.info("代码生成类型:{}", codeGenTypeEnum1);

        CodeGenTypeEnum codeGenTypeEnum2 = aiCodeGenTypeRouterService.routeCodeGenType("做一个公司官网，需要首页，联系我们，产品介绍三个页面");
        log.info("代码生成类型:{}", codeGenTypeEnum2);

        CodeGenTypeEnum codeGenTypeEnum3 = aiCodeGenTypeRouterService.routeCodeGenType("做一个图书管理页面，要求实现路由管理、图书管理、借阅管理等功能");
        log.info("代码生成类型:{}", codeGenTypeEnum3);

    }
}