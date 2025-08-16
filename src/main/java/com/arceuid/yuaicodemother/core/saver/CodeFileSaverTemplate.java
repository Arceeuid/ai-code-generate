package com.arceuid.yuaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.arceuid.yuaicodemother.constant.AppConstant;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器，模板方法模式
 *
 * @param <T> 代码文件保存器的参数类型
 */
public abstract class CodeFileSaverTemplate<T> {

    //文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 模板方法，保存代码标准流程
     *
     * @param result 代码文件保存器的参数
     * @param appId 应用ID
     * @return 代码文件保存的目录文件对象
     */
    public final File saveCode(T result, Long appId) {
        //1.验证输入
        validateInput(result);

        //2.构建唯一目录
        String uniqueDirPath = buildUniqueDir(appId);

        //3.保存文件
        saveFiles(result, uniqueDirPath);

        //4.返回目录文件对象
        return new File(uniqueDirPath);
    }

    //单个文件保存
    public final void saveSingleFile(String fileDir, String fileName, String content) {
        if (StrUtil.isNotBlank(content)) {
            String fileSavePath = fileDir + File.separator + fileName;
            FileUtil.writeString(content, fileSavePath, StandardCharsets.UTF_8);
        }
    }


    /**
     * 构建文件保存的唯一路径：tmp/code_output/type_雪花ID
     *
     * @param appId 应用ID
     * @return 代码文件保存的唯一路径
     */
    protected String buildUniqueDir(Long appId) {
        if (appId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        }
        String type = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", type, appId);
        String uniqueDirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(uniqueDirPath);
        return uniqueDirPath;
    }

    /**
     * 验证输入参数，子类可以重写
     *
     * @param result 代码文件保存器的参数
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码文件保存器参数不能为空");
        }
    }

    /**
     * 保存文件，交给子类实现
     *
     * @param result        代码文件保存器的参数
     * @param uniqueDirPath 代码文件保存的唯一路径
     */
    protected abstract void saveFiles(T result, String uniqueDirPath);

    /**
     * 获取代码类型，交给子类实现
     *
     * @return 代码类型
     */
    protected abstract CodeGenTypeEnum getCodeType();
}
