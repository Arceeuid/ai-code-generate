package com.arceuid.yuaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("多文件HTML,JS,CSS代码")
public class MultiFileCodeResult {
    /**
     * HTML代码
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * js代码
     */
    @Description("js代码")
    private String jsCode;

    /**
     * css代码
     */
    @Description("css代码")
    private String cssCode;

    /**
     * 代码描述
     */
    @Description("生成代码的描述")
    private String description;
}
