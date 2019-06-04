package com.juhex.sms.controller;

import com.juhex.sms.bean.PhoneDistrict;
import com.juhex.sms.bean.PhoneDistrictUtil;
import com.juhex.sms.service.CommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class FOFacade {

    //@Autowired
    //private CommonService service;

    @Autowired
    private PhoneDistrictUtil phoneDistrictUtil;

    private Logger logger;

    @PostConstruct
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
        logger.info("Rest controller initiated...");
    }

    @RequestMapping(method = {RequestMethod.GET}, path = {"/echo"})
    public String sayHi(@RequestParam(name = "greeting") String greeting) {
        logger.info("enter FO greeting...");
        //service.doService();
        //service.doJDBCDemo();

        PhoneDistrict pd = phoneDistrictUtil.fetchPhoneDistrictByBaiduAPI("18910050231");
        logger.info("Phone district province is : {}, city is : {}, type is {}",pd.getProv(),pd.getCity(),pd.getType());

        return "hi ".concat(greeting);
    }
}
