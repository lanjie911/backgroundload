package com.juhex.sms.config;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class EnvDetector {

    private String domain = "loan.juhedx.com";
    private String port = "";
    private String protocol = "http";

    @PostConstruct
    public void init(){
        if(!this.isLinuxOS()){
            domain = "127.0.0.1";
            port = ":8080";
        }
    }

    public String getAccessURL(String merchantName,String directory,String shortURL){
        //TODO 这里要依靠商户的不同目录生成测试和真实连接，目前不用实现
        String wholeURl = protocol + "://" + domain + port + "/"+directory+"/" + shortURL;
        return wholeURl;
    }

    public String getRedirectURL(String merchantName){
        //TODO 这里要依靠商户的不同目录生成测试和真实连接，目前不用实现
        String wholeURl = protocol + "://" + domain + port;
        return wholeURl;
    }

    public boolean isLinuxOS(){
        String osName = System.getProperty("os.name");
        if(osName != null && osName.contains("Linux")){
            return true;
        }
        return false;
    }

}
