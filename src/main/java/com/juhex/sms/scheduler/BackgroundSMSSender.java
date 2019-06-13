package com.juhex.sms.scheduler;

import com.juhex.sms.bean.*;
import com.juhex.sms.config.EnvDetector;
import com.juhex.sms.dao.SMSSendDAO;
import com.juhex.sms.mocker.DevEnvSMSSenderMocker;
import com.juhex.sms.util.SMSClient;
import com.juhex.sms.util.SMSRespPackageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

    @Autowired
    private DevEnvSMSSenderMocker devEnvSMSSenderMocker;

    private Logger logger;

    @PostConstruct
    public void init() {
        // 初始化两个发送验证码的短息线程足以
        this.senderExecutor = Executors.newFixedThreadPool(2);
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }


    public void submitSingleJob(final SMSJob job) {

        Runnable run = () -> {
            String result;
            String logTitle;
            if (envDetector.isLinuxOS()) {
                result = smsClient.sendSms(job.getUrl(), job.getAccount(), job.getPassword(), job.getMobile(), job.getContent(), job.getExtno(), job.getRt());
                logTitle = "SMS-RESULT";
            } else {
                result = devEnvSMSSenderMocker.mockSMSResp(job.getMobile());
                logTitle = "SMS-MOCK-RESULT";
            }
            logger.info("[{}] : {}", logTitle, result);
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
                String logTitle;
                if (envDetector.isLinuxOS()) {
                    result = smsClient.sendP2PSms(job.getUrl(), job.getAccount(), job.getPassword(), job.getContent(), job.getExtno(), job.getRt());
                    logTitle = "SMS-P2P-RESULT";
                } else {
                    String[] bds = job.getContent().split("\r");
                    result = devEnvSMSSenderMocker.mockSMSResp(bds);
                    logTitle = "P2P-MOCK-RESULT";
                }

                logger.info("[{}] : {}", logTitle, result);
                if (!"ERROR".equals(result)) {
                    SMSResp resp = smsRespPackageParser.parseSMSResultText(result);
                    List<SMSPackage> packages = resp.getList();
                    String[] contents = job.getContent().split("\r");
                    for (SMSPackage pack : packages) {
                        Long merchantId = job.getMerchantId();
                        String msg = extractContentFromEncodeURL(pack.getMobile(),contents);
                        smsSendDAO.insertMTCommand(merchantId, msg, resp.getStatus(), pack.getMobile(), pack.getMid(), pack.getResult());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        senderExecutor.submit(run);
    }

    private String extractContentFromEncodeURL(String mobile, String[] encodeContent){
        for(String content:encodeContent){
            if(content.startsWith(mobile)){
                String decodeContent = "";
                try {
                    decodeContent = URLDecoder.decode(content.split("#")[1],"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return decodeContent;
            }
        }
        return "";
    }

}
