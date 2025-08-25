package com.arceuid.yuaicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.exception.ThrowUtils;
import com.arceuid.yuaicodemother.manager.CosManager;
import com.arceuid.yuaicodemother.service.ScreenShotService;
import com.arceuid.yuaicodemother.utils.WebScreenShotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class ScreenShotServiceImpl implements ScreenShotService {

    @Resource
    private CosManager cosManager;

    /**
     * 生成截图并上传到COS,清理本地文件
     *
     * @param url 网页URL
     * @return 可访问地址
     */
    @Override
    public String generateAndUploadScreenShot(String url) {
        //校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "url不能为空");

        //生成截图
        String screenShotPath = WebScreenShotUtils.saveWebScreenShot(url);
        if (StrUtil.isBlank(screenShotPath)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "截图失败");
        }
        //上传截图
        try {
            String cosUrl = uploadScreenShot(screenShotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "上传失败");
            return cosUrl;
        } finally {
            //清理本地文件
            deleteScreenShot(screenShotPath);
        }
    }


    /**
     * 上传截图
     *
     * @param screenShotPath 截图路径
     * @return 可访问地址
     */
    private String uploadScreenShot(String screenShotPath) {
        if (StrUtil.isBlank(screenShotPath)) {
            return null;
        }
        File file = new File(screenShotPath);
        if (!file.exists()) {
            return null;
        }

        //生成对象CosKey
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String dateFormat = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String cosKey = String.format("screen/%s/%s", dateFormat, fileName);

        //上传文件
        return cosManager.uploadFile(cosKey, file);
    }


    private void deleteScreenShot(String screenShotPath) {
        File file = new File(screenShotPath);
        if (file.exists()) {
            boolean result = FileUtil.del(screenShotPath);
            if (result) {
                log.info("删除文件成功，{}", screenShotPath);
                File parentFile = file.getParentFile();
                //删除文件保存的父目录
                if (parentFile.isDirectory() && Objects.requireNonNull(parentFile.listFiles()).length == 0) {
                    boolean parentResult = parentFile.delete();
                    if (parentResult) {
                        log.info("删除目录成功，{}", parentFile.getPath());
                    }
                }
            }
        }
    }
}
