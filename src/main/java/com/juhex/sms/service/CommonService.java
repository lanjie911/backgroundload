package com.juhex.sms.service;

import com.juhex.sms.bean.DemonBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonService {

    @Autowired
    private DemonBean bean;

    public void doService() {
        bean.doSomething("haha");
        Long lx = bean.getCalc();
        System.out.println("lx == "+lx);
    }
}
