package com.szh.aicodebackend.core;

import cn.hutool.json.JSONUtil;
import com.szh.aicodebackend.ai.AiCodeGeneratorService;
import com.szh.aicodebackend.ai.AiCodeGeneratorServiceFactory;
import com.szh.aicodebackend.ai.model.HtmlCodeResult;
import com.szh.aicodebackend.ai.model.MultiFileCodeResult;
import com.szh.aicodebackend.ai.model.message.AiResponseMessage;
import com.szh.aicodebackend.ai.model.message.ToolExecutedMessage;
import com.szh.aicodebackend.ai.model.message.ToolRequestMessage;
import com.szh.aicodebackend.constant.AppConstant;
import com.szh.aicodebackend.core.builder.VueProjectBuilder;
import com.szh.aicodebackend.model.enums.CodeGenTypeEnum;
import com.szh.aicodebackend.core.parser.CodeParserExecutor;
import com.szh.aicodebackend.core.saver.CodeFileSaverExecutor;
import com.szh.aicodebackend.exception.BusinessException;
import com.szh.aicodebackend.exception.ErrorCode;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成器门面类，整合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

//    @Resource
//    private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
    * 生成代码并保存(统一入口)
    * @param userMessage 用户消息
    * @param codeGenType 代码生成类型
    * @return 保存后的文件
    */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType,Long appId){
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"代码生成类型不能为空");
        }
        if (appId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenType);
        return switch (codeGenType){
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持的代码生成类型:"+codeGenType.getValue());
        };
    }

    /**
    * 生成代码并保存(统一入口)(流式)
    * @param userMessage 用户消息
    * @param codeGenType 代码生成类型
    */
    public Flux<String> generateAndSaveCodeStream(String userMessage,CodeGenTypeEnum codeGenType,Long appId){
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"代码生成类型不能为空");
        }
        if (appId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenType);
        return switch (codeGenType){
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream,CodeGenTypeEnum.HTML,appId);

            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream,appId);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持的代码生成类型:"+codeGenType.getValue());
        };
    }


    private Flux<String> processCodeStream(Flux<String> codeStream , CodeGenTypeEnum codeGenType,Long appId){
        //代码返回完成后再保存
        StringBuilder code = new StringBuilder();
        //拼接代码
        return codeStream
                .doOnNext(code::append)
                .doOnComplete(() -> {
                    //返回完成后保存代码
                    try {
                        String completeCode = code.toString();
                        Object parserReault = CodeParserExecutor.executeParser(completeCode, codeGenType);
                        //保存代码
                        File fileDir = CodeFileSaverExecutor.executeSaver(parserReault, codeGenType,appId);
                        log.info("保存代码成功,保存路径为：{}",fileDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败：{}",e.getMessage());
                    }
                });
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream,Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        // 异步构造 Vue 项目
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }

}
