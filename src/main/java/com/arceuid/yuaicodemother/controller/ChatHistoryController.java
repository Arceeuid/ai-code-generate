package com.arceuid.yuaicodemother.controller;

import com.arceuid.yuaicodemother.annotation.AuthCheck;
import com.arceuid.yuaicodemother.common.BaseResponse;
import com.arceuid.yuaicodemother.common.ResultUtils;
import com.arceuid.yuaicodemother.constant.UserConstant;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.exception.ThrowUtils;
import com.arceuid.yuaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.arceuid.yuaicodemother.model.entity.ChatHistory;
import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.service.ChatHistoryService;
import com.arceuid.yuaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author arceuid
 * @since 2025-08-17
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 用户获取应用的对话历史
     *
     * @param appId          应用 id
     * @param pageSize       分页大小
     * @param lastCreateTime 最后创建时间
     * @param request        HttpServletRequest
     * @return 对话历史分页列表
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> ListAppChatHistory(@PathVariable("appId") Long appId,
                                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> page = chatHistoryService.getChatHistoryPage(appId, loginUser, pageSize, lastCreateTime);
        return ResultUtils.success(page);
    }

    /**
     * 管理员查询对话历史分页列表
     *
     * @param chatHistoryQueryRequest 对话历史查询请求
     * @return 对话历史分页列表
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/list/page/vo")
    public BaseResponse<Page<ChatHistory>> listChatHistoryPageByAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        //校验参数
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");

        //查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        Page<ChatHistory> result = chatHistoryService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }
}
