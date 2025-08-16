package com.arceuid.yuaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("应用名称结果")
public class AppNameResult {
    @Description("应用名称")
    private String appName;
}
