package com.juhex.sms.controller;

import com.juhex.sms.bean.DemonBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BossFacade {

    @Autowired
    private DemonBean demonBean;

    private Logger logger;

    public  BossFacade(){
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }

    @RequestMapping(method = {RequestMethod.GET}, path = {"/ping"})
    public String sayHi(@RequestParam(name = "gr") String greeting) {
        logger.info("IN BossFacade demon bean is {}",demonBean.getCalc().longValue());
        return "{\"rs\":\"OK\"}";

    }
}
