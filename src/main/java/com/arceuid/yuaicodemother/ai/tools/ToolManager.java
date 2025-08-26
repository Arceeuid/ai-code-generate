package com.arceuid.yuaicodemother.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ToolManager {
    @Resource
    private BaseTool[] tools;

    private final Map<String, BaseTool> toolMap = new HashMap<>();

    /**
     * 初始化工具，将注入的BaseTool加入Map中
     */
    @PostConstruct
    public void init() {
        for (BaseTool baseTool : tools) {
            toolMap.put(baseTool.getName(), baseTool);
            log.info("初始化工具: {}", baseTool.getName());
        }
    }

    /**
     * 根据名称获取工具
     *
     * @param name 工具名称
     * @return 工具
     */
    public BaseTool getToolByName(String name) {
        return toolMap.get(name);
    }

    /**
     * 获取所有工具
     *
     * @return 所有工具
     */
    public BaseTool[] getAllTools() {
        log.info("获取所有工具{}", toolMap);
        return tools;
    }
}
