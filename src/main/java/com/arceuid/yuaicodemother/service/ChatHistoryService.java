package com.arceuid.yuaicodemother.service;

import com.arceuid.yuaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.arceuid.yuaicodemother.model.entity.ChatHistory;
import com.arceuid.yuaicodemother.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author arceuid
 * @since 2025-08-17
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     *
     * @param appId       应用id
     * @param userId      用户id
     * @param message     消息
     * @param messageType 消息类型
     * @return
     */
    boolean addChatMessage(Long appId, Long userId, String message, String messageType);

    /**
     * 加载对话历史到redis
     *
     * @param appId           应用id
     * @param chatMemory      消息窗口
     * @param maxMessageCount 最大加载数量
     * @return 加载数量
     */
    int loadChatHistoryToMemory(long appId, MessageWindowChatMemory chatMemory, int maxMessageCount);

    /**
     * 根据应用id删除对话历史
     *
     * @param appId 应用id
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装器
     *
     * @param chatHistoryQueryRequest 对话历史查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 获取对话历史分页
     *
     * @param appId          应用id
     * @param loginUser      登录用户
     * @param pageSize       每页数量
     * @param lastCreateTime 最后创建时间
     * @return 对话历史分页
     */
    Page<ChatHistory> getChatHistoryPage(Long appId, User loginUser, Integer pageSize, LocalDateTime lastCreateTime);
}
