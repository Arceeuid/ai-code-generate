package com.arceuid.yuaicodemother.langgraph4j.tool;

import com.arceuid.yuaicodemother.langgraph4j.model.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    ImageSearchTool imageSearchTool;

    @Test
    void searchContentImages() {

        List<ImageResource> imageList = imageSearchTool.searchContentImages("technology");

        System.out.println((long) imageList.size());
    }
}