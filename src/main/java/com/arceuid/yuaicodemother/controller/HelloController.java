package com.arceuid.yuaicodemother.controller;

import com.arceuid.yuaicodemother.common.BaseResponse;
import com.arceuid.yuaicodemother.common.ResultUtils;
import com.arceuid.yuaicodemother.ratelimter.annatation.RateLimit;
import com.arceuid.yuaicodemother.ratelimter.enums.RateLimitType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @RateLimit(rate = 1, rateInterval = 60, limitType = RateLimitType.API)
    @GetMapping()
    public BaseResponse<String> hello() {
        return ResultUtils.success("hello");
    }
}
