package com.arceuid.yuaicodemother.controller;

import com.arceuid.yuaicodemother.common.BaseResponse;
import com.arceuid.yuaicodemother.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping()
    public BaseResponse<String> hello() {
        return ResultUtils.success("hello");
    }
}
