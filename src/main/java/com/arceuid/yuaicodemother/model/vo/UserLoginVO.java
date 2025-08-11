package com.arceuid.yuaicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录响应
 *
 * @author arceuid
 * @since 2025-08-11
 */
@Data
public class UserLoginVO implements Serializable {
    private static final long serialVersionID = 1L;
    /**
     * 用户id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 角色
     */
    private String userRole;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
