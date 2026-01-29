package com.szh.aicodebackend.constant;

public class RedisConstant {

    static final String GOOD_APP_KEY = "good_app:";


    static String getGoodAppKey(Long appId) {
        return GOOD_APP_KEY + appId;
    }
}
