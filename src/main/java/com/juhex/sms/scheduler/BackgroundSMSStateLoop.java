package com.juhex.sms.scheduler;

import com.juhex.sms.config.EnvDetector;
import com.juhex.sms.mocker.DevEnvSMSSenderMocker;
import com.juhex.sms.util.SMSClient;
import com.juhex.sms.bean.SMSPackage;
import com.juhex.sms.bean.SMSResp;
import com.juhex.sms.util.SMSRespPackageParser;
import com.juhex.sms.config.SMSConfig;
import com.juhex.sms.dao.SMSSendDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class BackgroundSMSStateLoop {

    private ScheduledExecutorService senderExecutor;

    @Autowired
    private SMSClient smsClient;

    @Autowired
    private SMSSendDAO smsSendDAO;

    @Autowired
    private SMSRespPackageParser smsRespPackageParser;

    @Autowired
    private SMSConfig smsConfig;

    @Autowired
    private EnvDetector envDetector;

    @Autowired
    private DevEnvSMSSenderMocker devEnvSMSSenderMocker;

    private Logger logger;

    private Runnable job;

    public BackgroundSMSStateLoop() {

    }

    private void initConfig(){
        String url = "http://" + smsConfig.get("vhost") + ":" + smsConfig.get("vport") + "/sms";
        String account = smsConfig.get("verify.account");
        String password = smsConfig.get("verify.password");

        job = () -> {
            String result;
            String logTitle;
            if(envDetector.isLinuxOS()){
                result = smsClient.queryReport(url, account, password, "json");
                logTitle = "SMS-REPORT";
            }else{
                result = devEnvSMSSenderMocker.mockSMSReport();
                logTitle = "SMS-MOCK-REPORT";
            }
            logger.info("[{}] : {}", logTitle, result);
            try {
                if (!"ERROR".equals(result)) {
                    // 发送成功入库
                    SMSResp resp = smsRespPackageParser.parseSMSResultText(result);
                    List<SMSPackage> ls = resp.getList();

                    for(SMSPackage pack:ls){
                        String mid = pack.getMid();
                        String mobile = pack.getMobile();
                        String stat = pack.getStat();
                        smsSendDAO.updateMTVCode(mobile,mid,stat);
                    }
                }
            } catch (Throwable a) {
                a.printStackTrace();
                //Going...ON... Can't stop
            }
        };
    }

    @PostConstruct
    public void init() {
        initConfig();
        // 初始化1个定期间隔线程
        this.senderExecutor = Executors.newScheduledThreadPool(1);
        senderExecutor.scheduleWithFixedDelay(job, 10, 15, TimeUnit.SECONDS);
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }
}
