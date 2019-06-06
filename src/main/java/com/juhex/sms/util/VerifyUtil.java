package com.juhex.sms.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class VerifyUtil {

    private String REGEX_MOBILE;

    private String REGEX_6_VCODE;

    public VerifyUtil(){
        REGEX_MOBILE = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        REGEX_6_VCODE = "^([0-9]){6,6}$";
    }

    // 验证手机号码
    public boolean isMobile(String mobile) {
        return Pattern.matches(REGEX_MOBILE, mobile);
    }

    // 验证6位验证码
    public boolean isVCode(String vcode){
        return Pattern.matches(REGEX_6_VCODE,vcode);
    }
}
