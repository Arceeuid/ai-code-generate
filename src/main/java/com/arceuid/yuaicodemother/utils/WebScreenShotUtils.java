package com.arceuid.yuaicodemother.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class WebScreenShotUtils {

    private static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    /**
     * 保存网页截图
     *
     * @param WebUrl 网页URL
     * @return 图片路径, 出错则返回null
     */
    public static String saveWebScreenShot(String WebUrl) {
        // 校验参数
        if (StrUtil.isBlank(WebUrl)) {
            log.error("网页URL不能为空");
            return null;
        }
        try {
            //创建截图保存的临时目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots" +
                    File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);

            //图片后缀
            final String IMG_SUFFIX = ".png";

            //原始图片路径
            String originPath = rootPath + File.separator + RandomUtil.randomString(6) + IMG_SUFFIX;

            //打开网页
            webDriver.get(WebUrl);

            // 等待页面加载完成
            waitForPageLoaded(webDriver);

            //截图并保存
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            saveImg(screenshotBytes, originPath);
            log.info("原始图片保存到{}成功", originPath);

            //压缩图片并保存
            final String COMPRESSION_SUFFIX = "_compressed.jpg";
            String compressionPath = rootPath + File.separator + RandomUtil.randomString(6) + COMPRESSION_SUFFIX;
            compressImg(originPath, compressionPath);
            log.info("压缩图片保存到{}成功", compressionPath);

            //删除原始图片
            FileUtil.del(originPath);
            log.info("原始图片{}已删除", originPath);

            //返回压缩后的图片路径
            return compressionPath;

        } catch (Exception e) {
            log.error("保存网页截图失败", e);
            return null;
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 保存图片
     *
     * @param imgBytes 图片字节数组
     * @param savePath 保存路径
     */
    private static void saveImg(byte[] imgBytes, String savePath) {
        try {
            FileUtil.writeBytes(imgBytes, savePath);
        } catch (Exception e) {
            log.error("保存图片失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param originPath   原始图片路径
     * @param compressPath 压缩后图片路径
     */
    private static void compressImg(String originPath, String compressPath) {
        // 压缩图片质量
        final float COMPRESS_QUALITY = 0.3f;

        try {
            ImgUtil.compress(FileUtil.file(originPath), FileUtil.file(compressPath), COMPRESS_QUALITY);
        } catch (Exception e) {
            log.error("压缩图片失败{} -> {}", originPath, compressPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    private static void waitForPageLoaded(WebDriver webDriver) {
        try {
            //创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            //等待document.readyState为complete
            wait.until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));

            //额外等待一段时间，确保页面完全加载
            Thread.sleep(2000);

            log.info("页面加载完成");

        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续进行截图", e);
        }
    }

}

