package com.arceuid.yuaicodemother.core.saver;

import com.arceuid.yuaicodemother.ai.model.HtmlCodeResult;
import com.arceuid.yuaicodemother.ai.model.MultiFileCodeResult;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存执行器
 * 负责执行代码文件保存操作
 */
public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();
    private static final MultiFileCodeSaverTemplate multiFileCodeSaverTemplate = new MultiFileCodeSaverTemplate();

    /**
     * 执行保存
     *
     * @param result          代码结果
     * @param codeGenTypeEnum 代码类型
     * @return 代码文件目录
     */
    public static File executeSave(Object result, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) result);
            case MULTI_FILE -> multiFileCodeSaverTemplate.saveCode((MultiFileCodeResult) result);
            default -> {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型：");
            }
        };
    }
}
