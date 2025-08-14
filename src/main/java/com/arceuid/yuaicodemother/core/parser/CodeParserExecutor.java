package com.arceuid.yuaicodemother.core.parser;

import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码执行解析器
 * 根据代码类型执行解析器的解析操作
 */
public class CodeParserExecutor {
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行解析
     *
     * @param codeContent     代码内容
     * @param codeGenTypeEnum 代码类型
     * @return 解析结果
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码类型");
        };
    }

}
