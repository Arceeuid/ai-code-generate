package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * 代码生成类型路由服务
 */
public interface AICodeGenTypeRouterService {
    /**
     * 路由代码生成类型
     *
     * @param userInput 用户输入
     * @return 代码生成类型
     */
    @SystemMessage(fromResource = "prompt/codegen-router-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userInput);
}
