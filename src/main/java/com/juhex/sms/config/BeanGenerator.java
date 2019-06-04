package com.juhex.sms.config;

import com.juhex.sms.bean.DemonBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanGenerator {

    @Bean
    public DemonBean getDemonBean(){
        return new DemonBean();
    }

}
