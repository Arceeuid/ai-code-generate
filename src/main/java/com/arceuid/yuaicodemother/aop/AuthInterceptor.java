package com.arceuid.yuaicodemother.aop;

import com.arceuid.yuaicodemother.annotation.AuthCheck;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.model.enums.UserRoleEnum;
import com.arceuid.yuaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 执行拦截，检查用户是否有权限
     *
     * @param joinPoint 连接点
     * @param authCheck 注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        //获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User user = userService.getLoginUser(request);

        //检查用户是否登录
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //不需要权限，直接通过
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(user.getUserRole());
        //没有权限，直接拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //管理员权限，直接通过
        if (userRoleEnum == UserRoleEnum.ADMIN) {
            return joinPoint.proceed();
        }

        //普通用户，检查角色是否符合
        if (userRoleEnum != mustRoleEnum) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //检查通过，放行
        return joinPoint.proceed();
    }
}
