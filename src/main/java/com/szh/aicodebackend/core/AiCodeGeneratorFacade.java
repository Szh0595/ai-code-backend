package com.szh.aicodebackend.core;

import com.szh.aicodebackend.ai.AiCodeGeneratorService;
import com.szh.aicodebackend.ai.AiCodeGeneratorServiceFactory;
import com.szh.aicodebackend.ai.model.HtmlCodeResult;
import com.szh.aicodebackend.ai.model.MultiFileCodeResult;
import com.szh.aicodebackend.exception.BusinessException;
import com.szh.aicodebackend.exception.ErrorCode;
import com.szh.aicodebackend.exception.ThrowUtils;
import com.szh.aicodebackend.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI代码生成器门面类(生成代码并保存)
 *
 * @author szh
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 生成代码并保存
     *
     * @param userMessage 用户输入
     * @param codeGenType 代码生成模式
     * @return 生成的代码文件
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "请选择代码生成模式");
        return switch (codeGenType) {
            case HTML -> generateAndAaveHtmlCode(userMessage, appId);
            case MULTI_FILE -> generateAndAaveMultiFileCode(userMessage, appId);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持的代码生成模式：" + codeGenType.getValue());
        };
    }

    /**
     * 生成HTML代码并保存
     *
     * @param userMessage 用户输入
     * @return 生成的HTML代码文件
     */
    public File generateAndAaveHtmlCode(String userMessage, Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult, appId);
    }

    /**
     * 生成多文件代码并保存
     *
     * @param userMessage 用户输入
     * @return 生成的多文件代码文件
     */
    public File generateAndAaveMultiFileCode(String userMessage, Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult, appId);
    }

    /**
     * 生成代码并保存(流式返回)
     *
     * @param userMessage 用户输入
     * @param codeGenType 代码生成模式
     * @return 生成的代码文件
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "请选择代码生成模式");
        return switch (codeGenType) {
            case HTML -> generateAndAaveHtmlCodeStream(userMessage, appId);
            case MULTI_FILE -> generateAndAaveMultiFileCodeStream(userMessage, appId);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持的代码生成模式：" + codeGenType.getValue());
        };
    }

    /**
     * 生成HTML代码并保存(流式返回)
     *
     * @param userMessage 用户输入
     * @return 生成的HTML代码文件
     */
    public Flux<String> generateAndAaveHtmlCodeStream(String userMessage, Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        //流式返回生成代码完成后再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(chunk ->{
            //实时收集代码片段
            codeBuilder.append(chunk);
                }
        ).doOnComplete(()-> {
            //流式返回完成后保存代码
            try {
                String completeHtmlCode = codeBuilder.toString();
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                //保存代码
                File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult, appId);
                log.info("保存成功，保存的目录：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存代码失败:{}", e.getMessage());
            }
        });
    }

    /**
     * 生成多文件代码并保存(流式返回)
     *
     * @param userMessage 用户输入
     * @return 生成的多文件代码文件
     */
    public Flux<String> generateAndAaveMultiFileCodeStream(String userMessage, Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //流式返回生成代码完成后再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(chunk ->{
                            //实时收集代码片段
                            codeBuilder.append(chunk);
                        }
                ).doOnComplete(()-> {
                    //流式返回完成后保存代码
                    try {
                        String completeMultiFileCode = codeBuilder.toString();
                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                        //保存代码
                        File savedDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult, appId);
                        log.info("保存成功，保存的目录：" + savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存代码失败:{}", e.getMessage());
                    }
                });
    }

}
