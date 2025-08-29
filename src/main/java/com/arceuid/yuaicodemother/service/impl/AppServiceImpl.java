package com.arceuid.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.arceuid.yuaicodemother.ai.AICodeGenTypeRouterFactory;
import com.arceuid.yuaicodemother.ai.AICodeGenTypeRouterService;
import com.arceuid.yuaicodemother.ai.model.AppNameResult;
import com.arceuid.yuaicodemother.constant.AppConstant;
import com.arceuid.yuaicodemother.core.AICodeGeneratorFacade;
import com.arceuid.yuaicodemother.core.builder.VueProjectBuilder;
import com.arceuid.yuaicodemother.core.handler.StreamHandlerExecutor;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.exception.ThrowUtils;
import com.arceuid.yuaicodemother.mapper.AppMapper;
import com.arceuid.yuaicodemother.model.dto.app.AppAddRequest;
import com.arceuid.yuaicodemother.model.dto.app.AppQueryRequest;
import com.arceuid.yuaicodemother.model.entity.App;
import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.arceuid.yuaicodemother.model.vo.AppVO;
import com.arceuid.yuaicodemother.model.vo.UserVO;
import com.arceuid.yuaicodemother.service.AppService;
import com.arceuid.yuaicodemother.service.ChatHistoryService;
import com.arceuid.yuaicodemother.service.ScreenShotService;
import com.arceuid.yuaicodemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author arceuid
 * @since 2025-08-14
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    ExecutorService executorService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenShotService screenShotService;

    @Resource
    private AICodeGenTypeRouterFactory aiCodeGenTypeRouterFactory;

    /**
     * 通过对话生成应用代码
     *
     * @param appId     应用 id
     * @param message   提示词
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //1.校验参数
        if (appId == null || message == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        //2.查询应用信息
        App app = getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }

        //3.校验用户是否有访问权限，只有本人可以和该应用对话
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限访问");
        }

        //4.获取生成的应用类型
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用类型不存在");
        }

        //5.保存用户消息到数据库
        chatHistoryService.addChatMessage(appId, loginUser.getId(), message, ChatHistoryMessageTypeEnum.USER.getValue());

        //6.生成并收集AI返回的代码流
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveStream(message, codeGenTypeEnum, appId);
        return streamHandlerExecutor.doExecute(contentFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum);

    }

    /**
     * 部署应用
     *
     * @param appId     应用 id
     * @param loginUser 登录用户
     * @return 可访问的部署地址
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        //1.校验参数
        if (appId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        //2.查询应用信息
        App app = getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }

        //3.校验用户是否有访问权限，只有本人可以部署该应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }

        //4.校验应用是否已部署,没有则生成6位字母+数字
        String deployKey = app.getDeployKey();
        if (app.getDeployKey() == null) {
            deployKey = RandomUtil.randomString(6);
        }

        //5.获取代码生成类型，构造原始代码路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + '_' + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);

        //6.检查路径是否存在
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码目录不存在");
        }

        //7.对Vue项目进行构建
        if (codeGenType.equals(CodeGenTypeEnum.VUE_PROJECT.getValue())) {
            boolean buildResult = vueProjectBuilder.buildProject(sourceDirPath);
            if (!buildResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用构建失败，请稍后重试");
            }
            File dist = new File(sourceDirPath + File.separator + "dist");
            //检查dist目录是否正常生成
            if (!dist.exists() || !dist.isDirectory()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用构建失败，请稍后重试");
            }
            sourceDir = dist;
        }

        //8.复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败，请稍后重试");
        }

        //9.更新数据库
        app.setDeployKey(deployKey);
        app.setDeployedTime(LocalDateTime.now());
        boolean updateSuccess = updateById(app);
        ThrowUtils.throwIf(!updateSuccess, ErrorCode.OPERATION_ERROR, "更新应用信息失败");

        //10.得到部署地址
        String deployUrl = AppConstant.CODE_DEPLOY_HOST + File.separator + deployKey;

        //11.异步生成截图
        generateAndUploadScreenShotAsync(appId, deployUrl);

        //12.返回部署地址
        return deployUrl;
    }

    /**
     * 添加应用
     *
     * @param appAddRequest 添加应用请求
     * @param loginUser     登录用户
     * @return 应用id
     */
    @Override
    public Long addApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");

        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());

        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));

        //获取应用类型
        AICodeGenTypeRouterService aiCodeGenTypeRouterService = aiCodeGenTypeRouterFactory.createAiCodeGenTypeRouterService();
        CodeGenTypeEnum selectCodeGenTypeEnum = aiCodeGenTypeRouterService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectCodeGenTypeEnum.getValue());

        // 插入数据库
        boolean result = save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 异步生成并更新应用名称
        generateAndUpdateAppName(app.getId());

        //返回应用id
        return app.getId();
    }

    /**
     * 异步生成截图并上传到COS
     *
     * @param appId 应用id
     * @param url   应用部署地址
     */
    @Override
    public void generateAndUploadScreenShotAsync(Long appId, String url) {
        Thread.startVirtualThread(() -> {
            String cosUrl = screenShotService.generateAndUploadScreenShot(url);

            App newApp = new App();
            newApp.setId(appId);
            newApp.setCover(cosUrl);
            updateById(newApp);
        });
    }

    /**
     * 根据前端请求参数构建查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName == null || appName.isEmpty() ? null : appName)
                .like("cover", cover == null || cover.isEmpty() ? null : cover)
                .like("initPrompt", initPrompt == null || initPrompt.isEmpty() ? null : initPrompt)
                .eq("codeGenType", codeGenType == null || codeGenType.isEmpty() ? null : codeGenType)
                .eq("deployKey", deployKey == null || deployKey.isEmpty() ? null : deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 转换应用实体列表为应用视图对象列表,并脱敏用户信息和应用信息
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * 转换应用实体为应用视图对象,并脱敏用户信息和应用信息
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    /**
     * 生成应用名称并更新应用信息
     *
     * @param appId 应用 id
     */
    public void generateAndUpdateAppName(Long appId) {
        App app = getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        executorService.submit(() -> {
            AppNameResult appNameResult = aiCodeGeneratorFacade.generateAppName(app.getInitPrompt());
            app.setAppName(appNameResult.getAppName());
            boolean updateSuccess = updateById(app);
            ThrowUtils.throwIf(!updateSuccess, ErrorCode.OPERATION_ERROR, "更新应用信息失败");
        });
    }

    @Override
    public boolean removeById(Serializable id) {
        //校验参数
        if (id == null) {
            return false;
        }
        Long appId = Long.parseLong(id.toString());

        //删除对话记录
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用对话记录失败", e);
        }

        //删除应用
        return super.removeById(appId);
    }

}
