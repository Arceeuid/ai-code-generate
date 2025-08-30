package com.arceuid.yuaicodemother.core;

import cn.hutool.json.JSONUtil;
import com.arceuid.yuaicodemother.ai.AICodeGeneratorService;
import com.arceuid.yuaicodemother.ai.AICodeGeneratorServiceFactory;
import com.arceuid.yuaicodemother.ai.AICodeNameGeneratorService;
import com.arceuid.yuaicodemother.ai.AICodeNameServiceFactory;
import com.arceuid.yuaicodemother.ai.model.AppNameResult;
import com.arceuid.yuaicodemother.ai.model.HtmlCodeResult;
import com.arceuid.yuaicodemother.ai.model.MultiFileCodeResult;
import com.arceuid.yuaicodemother.ai.model.message.AiResponseMessage;
import com.arceuid.yuaicodemother.ai.model.message.ToolExecutedMessage;
import com.arceuid.yuaicodemother.ai.model.message.ToolRequestMessage;
import com.arceuid.yuaicodemother.constant.AppConstant;
import com.arceuid.yuaicodemother.core.builder.VueProjectBuilder;
import com.arceuid.yuaicodemother.core.parser.CodeParserExecutor;
import com.arceuid.yuaicodemother.core.saver.CodeFileSaverExecutor;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成门面类，组合生成和保存的功能
 */
@Service
@Slf4j
public class AICodeGeneratorFacade {

    /**
     * 代码生成服务工厂
     */
    @Resource
    private AICodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private AICodeNameServiceFactory aiCodeNameServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;


    /**
     * 生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用ID
     * @return 代码文件目录
     */
    public File generateAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        AICodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSave(htmlCodeResult, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSave(multiFileCodeResult, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum;
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成并保存代码(流式)
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用ID
     * @return 代码文件目录
     */
    public Flux<String> generateAndSaveStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        AICodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum;
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 处理TokenStream，将其转为Flux<String>
     *
     * @param tokenStream TokenStream
     * @return Flux流
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        //同步构建Vue项目
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);

                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


    /**
     * 通用处理代码流方法
     *
     * @param result          代码流
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用ID
     * @return 代码文件目录
     */
    private Flux<String> processCodeStream(Flux<String> result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        //定义一个StringBuilder，用于拼接result流
        StringBuilder stringBuilder = new StringBuilder();
        return result.doOnNext(stringBuilder::append)
                .doOnComplete(() -> {
                    try {
                        //将StringBuilder转为对象
                        Object parsedCodeResult = CodeParserExecutor.executeParser(stringBuilder.toString(), codeGenTypeEnum);
                        //将对象保存到文件
                        File saveDir = CodeFileSaverExecutor.executeSave(parsedCodeResult, codeGenTypeEnum, appId);
                        log.info("保存到{}成功", saveDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存代码失败,{}", e.getMessage());
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存代码失败");
                    }
                });
    }

    /**
     * 生成应用名称
     *
     * @param userMessage 用户提示词
     * @return 应用名称
     */
    public AppNameResult generateAppName(String userMessage) {
        AICodeNameGeneratorService aiCodeNameGeneratorService = aiCodeNameServiceFactory.createAiCodeNameGeneratorService();
        return aiCodeNameGeneratorService.generateAppName(userMessage);
    }
}
