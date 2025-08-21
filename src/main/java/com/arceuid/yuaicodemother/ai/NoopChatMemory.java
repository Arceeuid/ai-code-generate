package com.arceuid.yuaicodemother.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoopChatMemory implements ChatMemory {

    private static final NoopChatMemory INSTANCE = new NoopChatMemory();

    private List<ChatMessage> messages;

    //单例模式，避免重复创建
    private NoopChatMemory() {
        messages = new ArrayList<>();
    }

    public static NoopChatMemory getInstance() {
        return INSTANCE;
    }

    @Override
    public Object id() {
        return null;
    }

    //每次添加消息前先清空消息列表，避免存储记忆
    @Override
    public void add(ChatMessage message) {
        clear();

        //防止重复添加系统消息
        if (message instanceof SystemMessage) {
            //已经包含直接返回
            if (messages.contains(message)) {
                return;
            }
            //不包含直接替换系统消息
            messages.clear();
        }

        messages.add(message);
    }

    @Override
    public List<ChatMessage> messages() {
        return messages;
    }

    //过滤掉用户消息和Ai消息，只保留系统消息
    @Override
    public void clear() {
        messages = messages.stream()
                .filter(message -> message instanceof SystemMessage)
                .collect(Collectors.toList());
    }
}
