package com.arceuid.yuaicodemother.core.saver;

import com.arceuid.yuaicodemother.ai.model.MultiFileCodeResult;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存模板
 */
public class MultiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected void saveFiles(MultiFileCodeResult result, String uniqueDirPath) {
        saveSingleFile(uniqueDirPath, "index.html", result.getHtmlCode());
        saveSingleFile(uniqueDirPath, "script.js", result.getJsCode());
        saveSingleFile(uniqueDirPath, "style.css", result.getCssCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        if (result.getHtmlCode() == null || result.getHtmlCode().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "多文件代码中的HTML文件不能为空");
        }
        if (result.getJsCode() == null || result.getJsCode().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "多文件代码中的JS文件不能为空");
        }
        if (result.getCssCode() == null || result.getCssCode().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "多文件代码中的CSS文件不能为空");
        }
    }
}
