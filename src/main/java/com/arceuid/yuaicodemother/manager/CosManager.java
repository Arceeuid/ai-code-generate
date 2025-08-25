package com.arceuid.yuaicodemother.manager;

import com.arceuid.yuaicodemother.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class CosManager {

    @Resource
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 存入对象存储
     *
     * @param key  文件名
     * @param file 文件对象
     * @return 上传结果
     */
    private PutObjectResult putObject(String key, File file) {
        String newKey = String.format("%s/%s", cosClientConfig.getFolderName(), key);
        PutObjectResult result = cosClient.putObject(cosClientConfig.getBucket(), newKey, file);
        return result;
    }

    /**
     * 上传文件
     *
     * @param key  文件名
     * @param file 文件对象
     * @return 上传结果
     */
    public String uploadFile(String key, File file) {
        PutObjectResult putObjectResult = putObject(key, file);
        if (putObjectResult != null) {
            String url = String.format("%s/%s/%s", cosClientConfig.getHost(), cosClientConfig.getFolderName(), key);
            log.info("上传文件到COS成功，{} -> {}", file.getName(), url);
            return url;
        }
        log.error("上传文件到COS失败，{}，返回结果为null", file.getName());
        return null;
    }
}
