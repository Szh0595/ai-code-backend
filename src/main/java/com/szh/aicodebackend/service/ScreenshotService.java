package com.szh.aicodebackend.service;

/**
 * 截图服务
 */
public interface ScreenshotService {


    /**
     * 生成网页截图并上传到 COS
     *
     * @param webUrl 网页地址
     * @return 截图的访问 URL
     */
    public String generateAndUploadScreenshot(String webUrl);

}
