package com.arceuid.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.mapper.UserMapper;
import com.arceuid.yuaicodemother.model.dto.user.UserLoginRequest;
import com.arceuid.yuaicodemother.model.dto.user.UserQueryRequest;
import com.arceuid.yuaicodemother.model.dto.user.UserRegisterRequest;
import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.model.enums.UserRoleEnum;
import com.arceuid.yuaicodemother.model.vo.UserLoginVO;
import com.arceuid.yuaicodemother.model.vo.UserVO;
import com.arceuid.yuaicodemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.arceuid.yuaicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author arceuid
 * @since 2025-08-11
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        //1.校验参数
        if (StrUtil.hasBlank(userRegisterRequest.getUserAccount(), userRegisterRequest.getUserPassword(), userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userRegisterRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if (userRegisterRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }

        //2.查询用户是否已存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userRegisterRequest.getUserAccount());
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        //3.加密密码
        String encryptPassword = getEncryptPassword(userRegisterRequest.getUserPassword());

        //4.新增用户
        User user = new User();
        user.setUserAccount(userRegisterRequest.getUserAccount());
        user.setUserPassword(encryptPassword);
        user.setUserName("Unknown");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录
     * @param request          HttpServletRequest
     * @return 用户登录VO
     */
    @Override
    public UserLoginVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        //1.校验参数
        if (StrUtil.hasBlank(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userLoginRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if (userLoginRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }

        //2.密码加密
        String encryptPassword = getEncryptPassword(userLoginRequest.getUserPassword());

        //3.查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userLoginRequest.getUserAccount());
        queryWrapper.eq(User::getUserPassword, encryptPassword);
        queryWrapper.eq(User::getIsDelete, 0);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //4.如果用户存在，记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        //5.返回脱敏后的用户信息
        return getLoginVO(user);
    }

    /**
     * 密码加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String password) {
        //盐值，混淆密码
        final String SALT = "arceuid";
        return DigestUtils.md5DigestAsHex((password + SALT).getBytes());
    }

    /**
     * 脱敏数据
     *
     * @param user 用户
     * @return 用户登录VO
     */
    @Override
    public UserLoginVO getLoginVO(User user) {
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtil.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    /**
     * 获取登录用户
     *
     * @param request HttpServletRequest
     * @return 用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //1.判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //2.从数据库查询用户
        user = this.getById(user.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        List<UserVO> userVOList = userList.stream()
                .map(this::getUserVO)
                .toList();
        return userVOList;
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        return QueryWrapper.create()
                .eq(User::getId, userQueryRequest.getId())
                .eq(User::getUserRole, userQueryRequest.getUserRole())
                .like(User::getUserAccount, userQueryRequest.getUserAccount())
                .like(User::getUserName, userQueryRequest.getUserName())
                .like(User::getUserProfile, userQueryRequest.getUserProfile())
                .orderBy(userQueryRequest.getSortField(), userQueryRequest.getSortOrder().equals("ascend"));
    }

    /**
     * 用户注销
     *
     * @param request HttpServletRequest
     * @return 是否注销成功
     */
    @Override
    public Boolean userLogout(HttpServletRequest request) {
        //1.判断用户是否登录
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        //2.注销用户登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

}
