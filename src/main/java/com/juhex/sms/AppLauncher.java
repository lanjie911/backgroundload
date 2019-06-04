/**
 * springboot的静态资源，默认路径按照优先级顺序是</p>
 * /META-INF/resources/
 * /resources/
 * /static/
 * /public/
 * 默认这些文件夹都建立在工程的resources原文件夹下即可
 */
package com.juhex.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppLauncher {
    public static void main(String[] args) {
        SpringApplication.run(AppLauncher.class,args);
    }
}
