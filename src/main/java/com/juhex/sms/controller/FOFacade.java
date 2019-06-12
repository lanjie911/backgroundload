package com.juhex.sms.controller;

import com.juhex.sms.bean.*;
import com.juhex.sms.service.MerchantService;
import com.juhex.sms.service.SMSSendService;
import com.juhex.sms.util.HttpJSONResponseWriter;
import com.juhex.sms.util.PhoneDistrictUtil;
import com.juhex.sms.util.VerifyUtil;
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

    @Autowired
    private DemonBean demonBean;

    @Autowired
    private PhoneDistrictUtil phoneDistrictUtil;

    @Autowired
    private VerifyUtil verifyUtil;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private SMSSendService smsSendService;

    private Logger logger;

    @PostConstruct
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
        logger.info("Rest controller initiated...");
    }

    // 发送注册短信
    @RequestMapping(method = {RequestMethod.GET}, path = {"/sendSMS"})
    public String sendSMS(@RequestParam(name = "q") String phone,@RequestParam(name = "keycode") String key, @RequestParam(name="surl") String shortURL){
        logger.info("Sending verify code , mobile is {}",phone);

        Map<String,String> resultMap = new HashMap<>();

        HttpJSONResponseWriter<Map> writer = new HttpJSONResponseWriter<>();

        if(!verifyUtil.isMobile(phone)){
            resultMap.put("rs","ERR");
            resultMap.put("text","非法的手机号");
            return writer.generate(resultMap);
        }

        // 通过key code找到商户才给发
        Merchant m = merchantService.getMerchantByKeycode(key);
        if(m == null){
            logger.info("Before send SMS, can't find merchant!");
            resultMap.put("rs","ERR");
            resultMap.put("text","非法的发送请求");
            return writer.generate(resultMap);
        }

        // 防止短信炸弹开始
        // 手机号码要和短连接匹配
        boolean isMatch = smsSendService.isSMSMatchedShortLink(phone,shortURL);
        if(!isMatch){
            logger.info("Before send SMS, phone can't match short URL!");
            resultMap.put("rs","ERR");
            resultMap.put("text","非法的手机号来源");
            return writer.generate(resultMap);
        }

        // 验证短信是否可以被发送
        // 是否在黑名单中
        // 是否达到了最大发送次数
        boolean canSend = smsSendService.isSMSCouldBeDelivered(phone,m);
        if(!canSend){
            logger.info("Before send SMS, can't send by requirement!");
            resultMap.put("rs","ERR");
            resultMap.put("text","发送次数过多");
            return writer.generate(resultMap);
        }

        // 此处是发送真的验证码短信
        // 由于是异步发送，所以不关注结果
        // 只要不抛出异常即可
        smsSendService.sendVerifyCodeSMS(phone,m);

        resultMap.put("rs","OK");
        resultMap.put("text","发送验证码成功");
        return writer.generate(resultMap);
    }
}
