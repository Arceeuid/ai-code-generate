package com.arceuid.yuaicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *
 * @author arceuid
 * @since 2025-08-11
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionID = 1L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
