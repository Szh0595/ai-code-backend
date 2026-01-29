package com.szh.aicodebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.szh.aicodebackend.exception.BusinessException;
import com.szh.aicodebackend.exception.ErrorCode;
import com.szh.aicodebackend.exception.ThrowUtils;
import com.szh.aicodebackend.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要忽略的目录名和文件名
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要忽略的扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );
    private boolean isPathAllowed(Path projectRoot, Path fullPath){
        //获取相对路径
        Path relativePath = projectRoot.relativize(fullPath);
        for (Path part:relativePath) {
            String pathName = part.toString();
            //检查是否在忽略名单中
            if(IGNORED_NAMES.contains(pathName)){
                return false;
            }
            if(IGNORED_EXTENSIONS.stream().anyMatch(pathName::endsWith)){
                return false;
            }
        }
        return true;
    }
    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        //参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR,"项目路径不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR,"下载文件名不能为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.PARAMS_ERROR,"项目路径不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR,"项目路径不是目录");
        log.info("开始下载项目：{}", projectPath);
        //设置HTTP响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadFileName));
        //定义文件过滤器
         FileFilter fileFilter = file -> {
            Path fullPath = file.toPath();
            return isPathAllowed(projectDir.toPath(), fullPath);
        };
        try {
            //将项目压缩到响应输出流
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8,false,fileFilter, projectDir);
            log.info("项目已下载完成：{}", projectPath);
        } catch (IOException e) {
            log.error("下载项目时发生错误：{}", projectPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载项目时发生错误");
        }
    }
}
