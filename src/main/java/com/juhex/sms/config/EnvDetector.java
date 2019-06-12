package com.juhex.sms.config;

import org.springframework.stereotype.Component;

@Component
public class EnvDetector {

    public boolean isLinuxOS(){
        String osName = System.getProperty("os.name");
        if(osName != null && osName.contains("Linux")){
            return true;
        }
        return false;
    }

}
