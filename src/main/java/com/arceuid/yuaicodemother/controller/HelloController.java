package com.arceuid.yuaicodemother.controller;

import com.arceuid.yuaicodemother.comon.BaseResponse;
import com.arceuid.yuaicodemother.comon.ResultUtils;
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
