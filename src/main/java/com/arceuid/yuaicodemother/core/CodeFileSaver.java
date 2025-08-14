package com.arceuid.yuaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.arceuid.yuaicodemother.ai.model.HtmlCodeResult;
import com.arceuid.yuaicodemother.ai.model.MultiFileCodeResult;
import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 代码文件保存器
 */
@Deprecated
public class CodeFileSaver {

    //文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    //单页面文件HTML保存
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String uniqueDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        saveSingleFile(uniqueDirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(uniqueDirPath);
    }

    //多文件保存
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String uniqueDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        saveSingleFile(uniqueDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        saveSingleFile(uniqueDirPath, "script.js", multiFileCodeResult.getJsCode());
        saveSingleFile(uniqueDirPath, "style.css", multiFileCodeResult.getCssCode());
        return new File(uniqueDirPath);
    }

    //构建文件保存的唯一路径：tmp/code_output/type_雪花ID
    private static String buildUniqueDir(String type) {
        String uniqueDirName = StrUtil.format("{}_{}", type, IdUtil.getSnowflakeNextIdStr());
        String uniqueDirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(uniqueDirPath);
        return uniqueDirPath;

    }

    //单个文件保存
    private static void saveSingleFile(String fileDir, String fileName, String content) {
        String fileSavePath = fileDir + File.separator + fileName;
        FileUtil.writeString(content, fileSavePath, StandardCharsets.UTF_8);
    }
}
