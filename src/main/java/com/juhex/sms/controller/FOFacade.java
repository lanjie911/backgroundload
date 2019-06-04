package com.juhex.sms.controller;

import com.juhex.sms.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class FOFacade {

    @Autowired
    private CommonService service;

    @PostConstruct
    public void init() {

        System.out.println("FO Facade initiated...");

    }

    @RequestMapping(method = {RequestMethod.GET}, path = {"/echo"})
    public String sayHi(@RequestParam(name = "greeting") String greeting) {
        System.out.println("enter greeting...");
        service.doService();
        service.doJDBCDemo();
        return "hi ".concat(greeting);
    }
}
