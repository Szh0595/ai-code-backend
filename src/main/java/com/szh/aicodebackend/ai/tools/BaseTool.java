package com.szh.aicodebackend.ai.tools;

import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * 基础工具类
 */
public abstract class BaseTool {
    /**
     * 获取工具英文名称
     * @return 工具英文名称
     */
    public abstract String getToolName();

    /**
     * 获取工具中文名称
     * @return 工具中文名称
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求响应
     * @return 工具请求响应
     */
    public String generateToolRequestResponse(){
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 生成工具执行结果(保存到数据库)
     * @param arguments 工具参数
     * @return 工具执行结果
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);

}
