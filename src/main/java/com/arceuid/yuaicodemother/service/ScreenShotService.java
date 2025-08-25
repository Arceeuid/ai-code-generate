package com.arceuid.yuaicodemother.service;


public interface ScreenShotService {

    /**
     * 生成截图并上传到COS
     *
     * @param url 网页URL
     * @return 可访问地址
     */
    String generateAndUploadScreenShot(String url);
}
