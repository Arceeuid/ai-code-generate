package com.arceuid.yuaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.exception.ThrowUtils;
import com.arceuid.yuaicodemother.mapper.ChatHistoryMapper;
import com.arceuid.yuaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.arceuid.yuaicodemother.model.entity.App;
import com.arceuid.yuaicodemother.model.entity.ChatHistory;
import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.arceuid.yuaicodemother.model.enums.UserRoleEnum;
import com.arceuid.yuaicodemother.service.AppService;
import com.arceuid.yuaicodemother.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author arceuid
 * @since 2025-08-17
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Lazy
    @Resource
    private AppService appService;

    /**
     * 添加对话历史
     *
     * @param appId       应用id
     * @param userId      用户id
     * @param message     消息
     * @param messageType 消息类型
     * @return
     */
    @Override
    public boolean addChatMessage(Long appId, Long userId, String message, String messageType) {
        //校验参数
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");

        //判断消息类型是否有效
        ThrowUtils.throwIf(ChatHistoryMessageTypeEnum.getEnumByValue(messageType) == null, ErrorCode.PARAMS_ERROR, "消息类型无效");

        //构造实体
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .build();
        return save(chatHistory);
    }

    /**
     * 加载对话历史到redis
     *
     * @param appId           应用id
     * @param chatMemory      消息窗口
     * @param maxMessageCount 最大加载数量
     * @return 加载数量
     */
    @Override
    public int loadChatHistoryToMemory(long appId, MessageWindowChatMemory chatMemory, int maxMessageCount) {
        try {
            //构建查询wrapper
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxMessageCount);

            //查询并反转列表
            List<ChatHistory> chatHistoryList = list(queryWrapper);

            //反转列表
            chatHistoryList = chatHistoryList.reversed();

            //添加进chatMemory
            int loadCount = 0;

            //清空chatMemory，防止重复加载
            chatMemory.clear();

            //添加进chatMemory
            for (ChatHistory chatHistory : chatHistoryList) {
                if (chatHistory.getMessageType().equals(ChatHistoryMessageTypeEnum.USER.getValue())) {
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                } else if (chatHistory.getMessageType().equals(ChatHistoryMessageTypeEnum.AI.getValue())) {
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                }
                loadCount++;
            }

            log.info("加载对话历史成功, appId: {}, 加载数量: {}", appId, loadCount);
            return loadCount;
        } catch (Exception e) {
            log.error("加载对话历史失败, appId: {}, 错误信息: {}", appId, e.getMessage());
            return 0;
        }
    }

    /**
     * 根据应用id删除对话历史
     *
     * @param appId 应用id
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByAppId(Long appId) {
        //校验参数
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用id不能为空");

        //根据应用id删除对话历史
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId);
        return remove(queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 对话历史查询请求
     * @return 查询包装类
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", StrUtil.isBlank(message) ? null : message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 获取对话历史分页列表,使用游标查询
     *
     * @param appId          应用id
     * @param loginUser      登录用户
     * @param pageSize       分页大小
     * @param lastCreateTime 最后创建时间
     * @return 对话历史分页列表
     */
    @Override
    public Page<ChatHistory> getChatHistoryPage(Long appId, User loginUser, Integer pageSize, LocalDateTime lastCreateTime) {
        //校验参数
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "用户不能为空");
        ThrowUtils.throwIf(pageSize == null, ErrorCode.PARAMS_ERROR, "分页大小不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 20, ErrorCode.PARAMS_ERROR, "分页大小不能超过20");

        //验证权限，只有管理员和本人可以查看
        App app = appService.getById(appId);
        Long userId = app.getUserId();
        String userRole = loginUser.getUserRole();
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(userRole);
        boolean isCreator = userId.equals(loginUser.getId());
        if (!isAdmin && !isCreator) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }
        //构造查询条件
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);

        //查询对话历史
        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);
        return page(new Page<>(1, pageSize), queryWrapper);
    }

}
