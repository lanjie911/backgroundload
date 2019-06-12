package com.juhex.sms.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juhex.sms.bean.*;
import com.juhex.sms.config.EnvDetector;
import com.juhex.sms.dao.SMSSendDAO;
import com.juhex.sms.util.SMSClient;
import com.juhex.sms.util.SMSRespPackageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BackgroundSMSSender {

    private ExecutorService senderExecutor;

    @Autowired
    private SMSClient smsClient;

    @Autowired
    private SMSSendDAO smsSendDAO;

    @Autowired
    private SMSRespPackageParser smsRespPackageParser;

    @Autowired
    private EnvDetector envDetector;

    private Logger logger;

    private ObjectMapper om = new ObjectMapper();

    @PostConstruct
    public void init() {
        // 初始化两个发送验证码的短息线程足以
        this.senderExecutor = Executors.newFixedThreadPool(2);
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }

    private String mockSMSResp(String mobile){

        SMSResp resp = new SMSResp();
        resp.setStatus(0);
        resp.setBalance(9999);

        List<SMSPackage> packages = new LinkedList<>();
        SMSPackage pack = new SMSPackage();
        pack.setMid(String.valueOf(System.currentTimeMillis()));
        pack.setMobile(mobile);
        pack.setResult(0);
        pack.setStat("0");
        packages.add(pack);
        resp.setList(packages);

        StringWriter sw = new StringWriter();
        try {
            om.writeValue(sw,resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sw.toString();
    }

    private String mockSMSResp(String[] phones){

        SMSResp resp = new SMSResp();
        resp.setStatus(0);
        resp.setBalance(9999);

        List<SMSPackage> packages = new LinkedList<>();

        for(String content:phones){
            SMSPackage pack = new SMSPackage();
            String prefix = String.valueOf(System.currentTimeMillis());
            String postfix = String.valueOf(Math.round(Math.random()* 100));
            pack.setMid(prefix.concat(postfix));

            String[] parts = content.split("#");
            String mobile = parts[0];

            pack.setMobile(mobile);
            pack.setResult(0);
            pack.setStat("0");
            packages.add(pack);
        }
        resp.setList(packages);

        StringWriter sw = new StringWriter();
        try {
            om.writeValue(sw,resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sw.toString();
    }

    public void submitSingleJob(final SMSJob job) {

        Runnable run = () -> {
            String result;
            if(envDetector.isLinuxOS()){
                result = smsClient.sendSms(job.getUrl(), job.getAccount(), job.getPassword(), job.getMobile(), job.getContent(), job.getExtno(), job.getRt());
            }else{
                result = this.mockSMSResp(job.getMobile());
            }
            logger.info("[SMS-RESULT] : {}", result);
            if (!"ERROR".equals(result)) {
                // 发送成功入库
                SMSResp resp = smsRespPackageParser.parseSMSResultText(result);
                SMSPackage pack = resp.getList().get(0);
                Long merchantId = job.getMerchantId();
                String msg = job.getContent();
                smsSendDAO.insertMTVCode(merchantId, msg, resp.getStatus(), job.getMobile(), pack.getMid(), pack.getResult());

                // 插入验证码库
                smsSendDAO.insertVCodeVerify(merchantId, job.getMobile(), job.getVcode());
            }
        };

        senderExecutor.submit(run);
    }

    public void submitP2PJob(final SMSJob job) {
        Runnable run = () -> {
            try {
                String result;
                if(envDetector.isLinuxOS()){
                    result = smsClient.sendP2PSms(job.getUrl(), job.getAccount(), job.getPassword(), job.getContent(), job.getExtno(), job.getRt());
                }else{
                    String[] bds = job.getContent().split("\r");
                    result = this.mockSMSResp(bds);
                }

                logger.info("[SMS-P2P-RESULT] : {}", result);
                if (!"ERROR".equals(result)) {
                    SMSResp resp = smsRespPackageParser.parseSMSResultText(result);
                    List<SMSPackage> packages = resp.getList();
                    for (SMSPackage pack : packages) {
                        Long merchantId = job.getMerchantId();
                        String msg = "*BATCH BINARY CONTENT*";
                        smsSendDAO.insertMTCommand(merchantId, msg, resp.getStatus(), pack.getMobile(), pack.getMid(), pack.getResult());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        senderExecutor.submit(run);
    }

}
