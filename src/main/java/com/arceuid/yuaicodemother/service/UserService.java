package com.arceuid.yuaicodemother.service;

import com.arceuid.yuaicodemother.model.dto.user.UserLoginRequest;
import com.arceuid.yuaicodemother.model.dto.user.UserQueryRequest;
import com.arceuid.yuaicodemother.model.dto.user.UserRegisterRequest;
import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.model.vo.UserLoginVO;
import com.arceuid.yuaicodemother.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author arceuid
 * @since 2025-08-11
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          HttpServletRequest
     * @return 用户登录VO
     */
    UserLoginVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 密码加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String password);

    /**
     * 脱敏数据
     *
     * @param user 用户
     * @return 用户登录VO
     */
    UserLoginVO getLoginVO(User user);

    /**
     * 获取登录用户
     *
     * @param request HttpServletRequest
     * @return 用户
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 获取脱敏后的用户VO
     *
     * @param user 用户
     * @return 用户VO
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户VO列表
     *
     * @param userList 用户列表
     * @return 用户VO列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 根据前端请求参数构建查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户注销
     *
     * @param request HttpServletRequest
     * @return 是否注销成功
     */
    Boolean userLogout(HttpServletRequest request);
}
