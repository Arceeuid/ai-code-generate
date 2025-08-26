package com.arceuid.yuaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.exception.ThrowUtils;
import com.arceuid.yuaicodemother.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;


@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    /**
     * 下载项目代码
     *
     * @param projectPath      项目路径
     * @param downloadFileName 下载文件名
     * @param response         返回响应
     */
    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        // 校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR, "下载文件名不能为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.PARAMS_ERROR, "项目路径不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是目录");

        // 设置请求头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadFileName));

        // 过滤文件
        FileFilter filter = file -> isAllowedPath(projectDir.toPath(), file.toPath());

        // 压缩文件
        try {
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
        } catch (IOException e) {
            log.error("项目打包下载失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目打包下载失败" + e.getMessage());
        }

    }

    /**
     * 检查路径是否允许下载
     *
     * @param projectRoot 项目根路径
     * @param fullPath    完整路径
     * @return 是否允许下载
     */
    private boolean isAllowedPath(Path projectRoot, Path fullPath) {
        //获取相对路径
        Path relativePath = projectRoot.relativize(fullPath);

        //检查路径中每一部分是否符合要求
        for (Path part : relativePath) {
            String partName = part.getFileName().toString();

            //检查文件名是否在忽略列表中
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }

            //检查文件扩展名是否在忽略列表中
            if (IGNORED_NAMES.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }

        return true;
    }
}


