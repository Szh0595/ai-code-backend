package com.szh.aicodebackend.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.szh.aicodebackend.exception.BusinessException;
import com.szh.aicodebackend.exception.ErrorCode;
import com.szh.aicodebackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;


/**
 * 代码保存模板
 *
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    public final File saveCode(T result,Long appId){
        //验证参数
        validateInput(result);
        //构建目录
        String baseDirPath = buildUniqueDir(appId);
        //保存代码
        saveFiles(result,baseDirPath);
        //返回保存的目录
        return new File(baseDirPath);
    }

    /**
     * 验证输入参数
     * @param result
     */
    protected abstract void validateInput(T result);

    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    protected final String buildUniqueDir(Long appId) {
        if (appId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        }
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}",codeType,appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    protected final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }
    /**
     * 获取代码类型(子类实现)
     * @return
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件(子类实现)
     * @param result
     * @param dirPath
     */
    protected abstract void saveFiles(T result, String dirPath);
}
