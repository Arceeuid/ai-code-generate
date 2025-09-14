package com.arceuid.yuaicodemother.service;

import com.arceuid.yuaicodemother.ai.model.AppNameResult;
import com.arceuid.yuaicodemother.model.dto.app.AppAddRequest;
import com.arceuid.yuaicodemother.model.dto.app.AppQueryRequest;
import com.arceuid.yuaicodemother.model.entity.App;
import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

/**
 * 应用 服务层。
 *
 * @author arceuid
 * @since 2025-08-14
 */
public interface AppService extends IService<App> {

    /**
     * 部署应用
     *
     * @param appId     应用 id
     * @param loginUser 登录用户
     * @return 可访问的部署地址
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 添加应用
     *
     * @param appAddRequest 添加应用请求
     * @param loginUser     登录用户
     * @return 应用id
     */
    Long addApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 异步生成截图并上传到COS
     *
     * @param appId 应用id
     * @param url   应用部署地址
     */
    void generateAndUploadScreenShotAsync(Long appId, String url);

    /**
     * 构造应用查询条件
     *
     * @param request 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest request);

    /**
     * 转换应用实体列表为应用视图对象列表,并脱敏用户信息和应用信息
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 转换应用实体为应用视图对象,并脱敏用户信息和应用信息
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 通过对话生成应用代码
     *
     * @param appId     应用 id
     * @param message   提示词
     * @param loginUser 登录用户
     * @return 应用代码
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 生成并更新应用名称
     *
     * @param appId 应用 id
     * @return
     */
    AppNameResult generateAppName(String initPrompt);

    /**
     * 删除应用
     *
     * @param appId 应用 id
     * @return 是否删除成功
     */
    boolean removeById(Serializable appId);
}
