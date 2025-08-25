package com.arceuid.yuaicodemother.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class WebScreenShotUtilsTest {

    @Test
    void saveWebScreenShot() {
        String webScreenShot = WebScreenShotUtils.saveWebScreenShot("https://www.bilibili.com");
        assertNotNull(webScreenShot);
    }
}