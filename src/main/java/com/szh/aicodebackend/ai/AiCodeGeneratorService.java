package com.szh.aicodebackend.ai;


import com.szh.aicodebackend.ai.model.HtmlCodeResult;
import com.szh.aicodebackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 生成HTML代码
     *
     * @param userMessage 用户输入
     * @return 生成的HTML代码
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户输入
     * @return 生成的多文件代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 流式生成HTML代码
     *
     * @param userMessage 用户输入
     * @return 生成的HTML代码
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 流式生成多文件代码
     *
     * @param userMessage 用户输入
     * @return 生成的多文件代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
