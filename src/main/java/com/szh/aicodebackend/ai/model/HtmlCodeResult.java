package com.szh.aicodebackend.ai.model;


import jdk.jfr.Description;
import lombok.Data;

@Data
public class HtmlCodeResult {

    @Description("生成的HTML代码")
    private String htmlCode;

    @Description("生成的HTML代码的描述")
    private String description;
}
