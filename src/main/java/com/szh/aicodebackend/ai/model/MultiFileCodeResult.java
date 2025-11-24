package com.szh.aicodebackend.ai.model;


import jdk.jfr.Description;
import lombok.Data;

@Data
public class MultiFileCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("CSS代码")
    private String cssCode;

    @Description("JS代码")
    private String jsCode;

    @Description("生成的代码描述")
    private String description;
}
