package com.szh.aicodebackend.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.szh.aicodebackend.ai.model.HtmlCodeResult;
import com.szh.aicodebackend.ai.model.MultiFileCodeResult;
import com.szh.aicodebackend.constant.AppConstant;
import com.szh.aicodebackend.exception.BusinessException;
import com.szh.aicodebackend.exception.ErrorCode;
import com.szh.aicodebackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class CodeFileSaver {
    // 文件保存根目录
    protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 保存 HtmlCodeResult
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result, Long appId) {
        String baseDirPath = buildUniqueDir(appId,CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 保存 MultiFileCodeResult
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result,Long appId) {
        String baseDirPath = buildUniqueDir(appId,CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    protected static String buildUniqueDir(Long appId, String codeType) {
        if (appId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        }
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    private static void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
