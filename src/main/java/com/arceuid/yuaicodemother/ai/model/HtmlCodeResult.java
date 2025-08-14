package com.arceuid.yuaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("单文件HTML代码")
public class HtmlCodeResult {
    /**
     * 单文件HTML代码
     */
    @Description("单文件HTML代码")
    private String htmlCode;

    /**
     * 文件描述
     */
    @Description("文件描述")
    private String description;
}
