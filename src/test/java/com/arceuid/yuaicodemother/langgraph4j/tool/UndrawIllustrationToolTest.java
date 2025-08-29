package com.arceuid.yuaicodemother.langgraph4j.tool;

import com.arceuid.yuaicodemother.langgraph4j.model.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class UndrawIllustrationToolTest {
    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Test
    void searchIllustrations() {
        List<ImageResource> imageList = undrawIllustrationTool.searchIllustrations("1222");
        System.out.println(imageList);
    }
}