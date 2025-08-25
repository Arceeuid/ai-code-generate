package com.arceuid.yuaicodemother.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 构建Vue项目
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 异步构建Vue项目
     *
     * @param projectPath 项目路径
     * @return 构建结果
     */
    public void buildProjectAsync(String projectPath) {
        Thread.ofVirtual().name("vue_project_builder_" + System.currentTimeMillis()).start(() -> {
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("异步构建Vue项目失败: {}", projectPath, e);
            }
        });
    }

    /**
     * 构建Vue项目
     *
     * @param projectPath 项目路径
     * @return 构建结果
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectPath);
            return false;
        }
        // 检查 package.json 是否存在
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在: {}", packageJson.getAbsolutePath());
            return false;
        }
        log.info("开始构建 Vue 项目: {}", projectPath);
        // 执行 npm install
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 执行失败");
            return false;
        }
        // 执行 npm run build
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build 执行失败");
            return false;
        }
        // 验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists()) {
            log.error("构建完成但 dist 目录未生成: {}", distDir.getAbsolutePath());
            return false;
        }
        log.info("Vue 项目构建成功，dist 目录: {}", distDir.getAbsolutePath());
        return true;
    }

    /**
     * 执行npm install
     *
     * @param workingDir 工作目录
     * @return 执行结果
     */
    private boolean executeNpmInstall(File workingDir) {
        log.info("执行npm install...");
        String npmCommand = isWindows() ? "npm.cmd" : "npm";
        return executeCommand(workingDir, npmCommand + " install", 300);
    }

    /**
     * 执行npm build
     *
     * @param workingDir 工作目录
     * @return 执行结果
     */
    private boolean executeNpmBuild(File workingDir) {
        log.info("执行npm run build...");
        String npmCommand = isWindows() ? "npm.cmd" : "npm";
        return executeCommand(workingDir, npmCommand + " run build", 180);
    }

    /**
     * 判断是否为Windows系统
     *
     * @return 结果
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令
     * @param timeoutSeconds 超时时间
     * @return 执行结果
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }
}
