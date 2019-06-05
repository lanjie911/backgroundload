package com.juhex.sms.controller;

import com.juhex.sms.bean.*;
import com.juhex.sms.service.CommonService;
import com.juhex.sms.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FOFacade {

    //@Autowired
    //private CommonService service;

    @Autowired
    private DemonBean demonBean;

    @Autowired
    private PhoneDistrictUtil phoneDistrictUtil;

    @Autowired
    private VerifyUtil verifyUtil;

    @Autowired
    private MerchantService merchantService;

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

        demonBean.increase();
        logger.info("demon bean increased : {}",demonBean.getCalc().longValue());

        PhoneDistrict pd = phoneDistrictUtil.fetchPhoneDistrictByBaiduAPI("18910050231");
        logger.info("Phone district province is : {}, city is : {}, type is {}",pd.getProv(),pd.getCity(),pd.getType());

        return "hi ".concat(greeting);
    }

    // 发送注册短信
    @RequestMapping(method = {RequestMethod.GET}, path = {"/sendSMS"})
    public String sendSMS(@RequestParam(name = "q") String phone,@RequestParam(name = "keycode") String key){
        logger.info("Sending verify code , mobile is {}",phone);

        Map<String,String> resultMap = new HashMap<>();

        HttpJSONResponseWriter<Map> writer = new HttpJSONResponseWriter<>();

        if(!verifyUtil.isMobile(phone)){
            resultMap.put("rs","ERR");
            resultMap.put("text","非法的手机号");
            return writer.generate(resultMap);
        }

        // 通过keycode找到商户才给发
        Merchant m = merchantService.getMerchantByKeycode(key);
        if(m == null){
            logger.info("Before send SMS, can't find merchant!");
            resultMap.put("rs","ERR");
            resultMap.put("text","非法的发送请求");
            return writer.generate(resultMap);
        }

        // 验证短信是否可以被发送
        // 是否在黑名单中
        // 是否达到了最大发送次数




        resultMap.put("rs","OK");
        resultMap.put("text","发送验证码成功");
        return writer.generate(resultMap);


    }
}
