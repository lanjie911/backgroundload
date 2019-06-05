package com.juhex.sms.config;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.util.Properties;

@Component(value = "smsConfig")
public class SMSConfig {

    private Properties prop;

    public Properties getProp() {
        return prop;
    }

    public SMSConfig(){
        prop = new Properties();
    }

    @PostConstruct
    public void init(){
        // 读取配置文件的关键
        String os = System.getProperty("os.name");
        String filePath = "";
        if(os.toLowerCase().contains("windows")){
            filePath = "d:/smskey.properties";
        }else if(os.toLowerCase().contains("linux")){
            filePath = "~/smskey.properties";
        }else{
            throw new Error("Unrecognized Operation System!!");
        }

        try(FileReader fr = new FileReader(filePath)){
            prop.load(fr);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public String get(String key){
        return this.prop.getProperty(key);
    }
}
