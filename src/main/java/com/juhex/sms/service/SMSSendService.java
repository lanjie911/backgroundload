package com.juhex.sms.service;

import com.juhex.sms.bean.*;
import com.juhex.sms.config.SMSConfig;
import com.juhex.sms.dao.MerchantDAO;
import com.juhex.sms.dao.SMSSendDAO;
import org.apache.http.cookie.SM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.util.Properties;

@Service(value = "smsSendService")
public class SMSSendService {

    @Autowired
    private SMSSendDAO smsSendDAO;

    @Autowired
    private MerchantDAO merchantDAO;

    @Autowired
    private VerifyCodeGenerator verifyCodeGenerator;

    @Autowired
    private BackgroudSMSSender backgroudSMSSender;

    private final Integer SMS_SEND_TIMES = 5;

    @Autowired
    private SMSConfig smsConfig;

    @PostConstruct
    public void init(){

    }

    public boolean isSMSCouldBeDelivered(String phone, Merchant merchant) {
        // 商户状态不对，不能注册，不能倒流
        if (merchant.getMerchantStatus() != 1) {
            return false;
        }

        // 号码是否在黑名单
        if (smsSendDAO.qryPhoneInBlackList(phone) > 0) {
            return false;
        }

        // 已经注册过了
        if (smsSendDAO.qryPhoneRegistered(phone, merchant.getMerchantId()) > 0) {
            return false;
        }

        // 已经发送总数超过5次了
        if (smsSendDAO.qryPhoneSendTimes(phone, merchant.getMerchantId()) > SMS_SEND_TIMES) {
            return false;
        }

        return true;

    }

    // 发送验证码短信
    // 记录商户信息
    public void sendVerifyCodeSMS(String phone,Merchant m){
        String vcode = verifyCodeGenerator.generateVerifyCode();

        // 根据商户取得验证码模板
        String smsTemplate = merchantDAO.qryTemplate(m.getMerchantId(),1);
        if(null == smsTemplate || "".equalsIgnoreCase(smsTemplate)){
            return;
        }
        String content = smsTemplate.replaceAll("\\{vcode\\}",vcode);

        SMSJob job = new SMSJob();
        job.setUrl("http://"+smsConfig.get("vhost")+":"+smsConfig.get("vport")+"/sms");
        job.setAccount(smsConfig.get("verify.acccount"));
        job.setPassword(smsConfig.get("verify.password"));
        job.setExtno(smsConfig.get("verify.vopernum"));
        job.setRt("json");
        job.setMobile(phone);
        job.setContent(content);
        job.setMerchantId(m.getMerchantId());

        backgroudSMSSender.submitSingleJob(job);
    }
}
