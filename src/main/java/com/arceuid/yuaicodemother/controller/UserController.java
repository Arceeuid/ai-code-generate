package com.arceuid.yuaicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import com.arceuid.yuaicodemother.annotation.AuthCheck;
import com.arceuid.yuaicodemother.common.BaseResponse;
import com.arceuid.yuaicodemother.common.DeleteRequest;
import com.arceuid.yuaicodemother.common.ResultUtils;
import com.arceuid.yuaicodemother.constant.UserConstant;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.exception.ThrowUtils;
import com.arceuid.yuaicodemother.model.dto.user.*;
import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.model.vo.UserLoginVO;
import com.arceuid.yuaicodemother.model.vo.UserVO;
import com.arceuid.yuaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author arceuid
 * @since 2025-08-11
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userRegister(userRegisterRequest));
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          HttpServletRequest
     * @return 用户登录VO
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userLogin(userLoginRequest, request));
    }

    /**
     * 用户注销
     *
     * @param request HttpServletRequest
     * @return 是否注销成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 获取登录用户
     *
     * @param request HttpServletRequest
     * @return 用户登录VO
     */
    @GetMapping("/get/login")
    public BaseResponse<UserLoginVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginVO(user));
    }

}
