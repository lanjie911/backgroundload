package com.juhex.sms.bean;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class VerifyCodeGenerator {

    // 生成六位数字验证码
    public String generateVerifyCode(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<6;i++){
            int number  = Math.abs(new Random().nextInt(10));
            sb.append(number);
        }
        return sb.toString();
    }
}
