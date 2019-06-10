package com.juhex.sms.scheduler;

import com.juhex.sms.bean.SMSPackage;
import com.juhex.sms.bean.SMSResp;
import com.juhex.sms.config.SMSConfig;
import com.juhex.sms.dao.SMSSendDAO;
import com.juhex.sms.util.SMSClient;
import com.juhex.sms.util.SMSRespPackageParser;
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
public class BackgroudMarketingStateLooper {

    private ScheduledExecutorService senderExecutor;

    @Autowired
    private SMSClient smsClient;

    @Autowired
    private SMSSendDAO smsSendDAO;

    @Autowired
    private SMSRespPackageParser smsRespPackageParser;

    @Autowired
    private SMSConfig smsConfig;

    private Logger logger;

    private Runnable job;

    public BackgroudMarketingStateLooper() {

    }

    private void initConfig(){
        String url = "http://" + smsConfig.get("vhost") + ":" + smsConfig.get("vport") + "/sms";
        String account = smsConfig.get("market.account");
        String password = smsConfig.get("market.password");

        job = () -> {

            String result = smsClient.queryReport(url, account, password, "json");
            logger.info("[MARKET-REPORT] : {}", result);
            try {
                if (!"ERROR".equals(result)) {
                    // 发送成功入库
                    SMSResp resp = smsRespPackageParser.parseSMSResultText(result);
                    List<SMSPackage> ls = resp.getList();

                    for(SMSPackage pack:ls){
                        String mid = pack.getMid();
                        String mobile = pack.getMobile();
                        String stat = pack.getStat();
                        smsSendDAO.updateMTCommand(mobile,mid,stat);
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
        senderExecutor.scheduleWithFixedDelay(job, 10, 10, TimeUnit.SECONDS);
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }
}
