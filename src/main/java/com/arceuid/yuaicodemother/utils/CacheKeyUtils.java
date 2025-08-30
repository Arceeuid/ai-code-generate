package com.arceuid.yuaicodemother.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 生成Cache的Key工具类
 */
public class CacheKeyUtils {

    /**
     * @param object
     * @return
     */
    public static String getCacheKey(Object object) {
        if (object == null) {
            return DigestUtil.md5Hex("null");
        }
        //转Json再转MD5
        String jsonStr = JSONUtil.toJsonStr(object);
        return DigestUtil.md5Hex(jsonStr);


    }


}
