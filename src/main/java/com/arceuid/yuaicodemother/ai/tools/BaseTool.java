package com.arceuid.yuaicodemother.ai.tools;

import cn.hutool.json.JSONObject;

public abstract class BaseTool {

    /**
     * 工具名称
     *
     * @return 工具名称
     */
    public abstract String getName();

    /**
     * 工具显示名称
     *
     * @return 工具显示名称
     */
    public abstract String getDisplayName();

    /**
     * 工具调用请求
     *
     * @return 工具调用请求
     */
    public String generateToolRequest() {
        return String.format("[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 工具执行请求
     *
     * @return 工具执行请求
     */
    public abstract String generateToolExecuteRequest(JSONObject arguments);

}
