package com.szh.aicodebackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.szh.aicodebackend.exception.ErrorCode;
import com.szh.aicodebackend.exception.ThrowUtils;
import com.szh.aicodebackend.manager.CosManager;
import com.szh.aicodebackend.service.ScreenshotService;
import com.szh.aicodebackend.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        //校验参数
        ThrowUtils.throwIf(webUrl == null, ErrorCode.PARAMS_ERROR,"网页url不能为空");
        log.info("开始截图：{}",webUrl);
        //截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(localScreenshotPath == null, ErrorCode.OPERATION_ERROR,"截图失败");
        log.info("截图成功：{}",localScreenshotPath);
        try {
            //上传到COS对象存储
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(cosUrl == null, ErrorCode.OPERATION_ERROR,"上传COS对象存储失败");
            log.info("上传COS对象存储成功：{}",cosUrl);
            return cosUrl;
        } finally {
            //删除本地文件
            cleanLocalFile(localScreenshotPath);
        }
    }
    /**
     * 上传截图到COS对象存储
     * @param localScreenshotPath
     * @return
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)){
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()){
            log.error("截图文件不存在：{}",localScreenshotPath);
            return null;
        }
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadFile(cosKey, screenshotFile);
    }

    /**
     * 生成截图的key
     * @param fileName
     * @return
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshot/%s/%s", datePath, fileName);
    }

    /**
     * 删除本地文件
     * @param localScreenshotPath
     */
    private void cleanLocalFile(String localScreenshotPath) {
        File screenshotFile = new File(localScreenshotPath);
        if (screenshotFile.exists()){
            File parentFile = screenshotFile.getParentFile();
            FileUtil.del(parentFile);
            log.info("删除本地文件成功：{}",parentFile.getAbsolutePath());
        }
    }
}
