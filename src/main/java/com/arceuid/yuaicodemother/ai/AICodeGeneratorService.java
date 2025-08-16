package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.ai.model.AppNameResult;
import com.arceuid.yuaicodemother.ai.model.HtmlCodeResult;
import com.arceuid.yuaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AICodeGeneratorService {
    /**
     * 生成HTML代码
     *
     * @param userMessage 用户提示词
     * @return 代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户提示词
     * @return 代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成应用名称
     *
     * @param userMessage 用户提示词
     * @return 应用名称
     */
    @SystemMessage(fromResource = "prompt/codegen-app-name-prompt")
    AppNameResult generateAppName(String userMessage);

    /**
     * 生成HTML代码，流式输出
     *
     * @param userMessage 用户提示词
     * @return 代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码，流式输出
     *
     * @param userMessage 用户提示词
     * @return 代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
