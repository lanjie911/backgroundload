package com.juhex.sms.scheduler;

import com.juhex.sms.bean.ForbiddenDistrict;
import com.juhex.sms.bean.PhoneDistrict;
import com.juhex.sms.bean.SMSJob;
import com.juhex.sms.bean.SMSTask;
import com.juhex.sms.config.SMSConfig;
import com.juhex.sms.dao.MerchantDAO;
import com.juhex.sms.dao.SMSSendDAO;
import com.juhex.sms.dao.SMSTaskDAO;
import com.juhex.sms.util.PhoneDistrictUtil;
import com.juhex.sms.util.ShortLinkGenerator;
import com.juhex.sms.util.VerifyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 背景发送批量营销短信</p>
 * 从指定任务表捞文件发送
 */
@Component
public class BackgroundBatchMarketingSender {

    // 这个是捞文件滴
    private ScheduledExecutorService fileLoader;

    private Runnable fileLoadTask;

    @Autowired
    private SMSTaskDAO smsTaskDAO;

    @Autowired
    private SMSSendDAO smsSendDAO;

    @Autowired
    private MerchantDAO merchantDAO;

    @Autowired
    private VerifyUtil verifyUtil;

    @Autowired
    private PhoneDistrictUtil phoneDistrictUtil;

    @Autowired
    private ShortLinkGenerator shortLinkGenerator;

    @Autowired
    private BackgroudSMSSender backgroudSMSSender;

    @Autowired
    private SMSConfig smsConfig;

    private Logger logger;

    @PostConstruct
    public void init() {

        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

        fileLoader = Executors.newScheduledThreadPool(1);

        fileLoadTask = () -> {
            try {
                SMSTask task = smsTaskDAO.qryOneSMSTask();
                if (task == null) {
                    logger.info("[SMS-MARKETING] : NO TASK FOUND.");
                    return;
                }
                logger.info("[SMS-MARKETING] task id is {}", task.getTaskId());

                //把任务状态改为处理中，防止重复抓取
                smsTaskDAO.updateSMSTaskState(task.getTaskId());

                String filePath = task.getFilePath();
                logger.info("[SMS-MARKETING] file path is {}", filePath);
                List<String> phoneList = this.getPhoneListFromFile(filePath);
                logger.info("[SMS-MARKETING] phone list size is {}", phoneList.size());
                String template = merchantDAO.qryTemplate(task.getMerchantId(), 2);
                logger.info("[SMS-MARKETING] phone template is {}", template);
                List<String> sendList = filterPhoneList(phoneList, task);
                logger.info("[SMS-MARKETING] after filter size is {}", sendList.size());
                //拆分发送
                int lastPosition = sendList.size();
                int begin = 0;
                int end = 101;
                String content = "";
                List<String> tempList = null;
                while(end < lastPosition){
                    logger.info("[SMS-MARKETING] sending marketing from {} to {}", begin, end-1);
                    tempList = sendList.subList(begin,end);
                    content = constructMarketingSMS(template, tempList, task);
                    submitMarketingSMSJob(task.getMerchantId(),content);
                    begin = end;
                    end += 100;
                }

                // 最后一批
                end = lastPosition;
                logger.info("[SMS-MARKETING] sending marketing from {} to {}", begin, end);
                tempList = sendList.subList(begin,end);
                content = constructMarketingSMS(template, tempList, task);
                submitMarketingSMSJob(task.getMerchantId(),content);

            }catch(Throwable a){
                a.printStackTrace();
            }
        };

        fileLoader.scheduleWithFixedDelay(fileLoadTask, 5, 10, TimeUnit.SECONDS);

        logger.info("[SMS-MARKETING] : initiated...");
    }

    private void submitMarketingSMSJob(Long merchantId,String content) {
        SMSJob job = new SMSJob();
        job.setUrl("http://" + smsConfig.get("vhost") + ":" + smsConfig.get("vport") + "/sms");
        job.setAccount(smsConfig.get("market.account"));
        job.setPassword(smsConfig.get("market.password"));
        job.setExtno(smsConfig.get("market.vopernum"));
        job.setRt("json");
        job.setContent(content);
        job.setMerchantId(merchantId);
        backgroudSMSSender.sumbitP2PJob(job);
    }

    private String constructMarketingSMS(String template, List<String> phoneList, SMSTask task) {

        Long merchantId = task.getMerchantId();
        Long taskId = task.getTaskId();

        StringBuilder sb = new StringBuilder();

        for (String phone : phoneList) {

            String shortURL = shortLinkGenerator.zipURL(merchantId + "&" + taskId + "&" + phone);
            logger.info("【Background Send】 phone is {}, short URL is {}",phone,shortURL);
            shortURL = "http://loan.juhedx.com/t/" + shortURL;
            String message = template.replaceAll("\\{slink\\}", shortURL);
            logger.info("【Background Send】 phone is {}, whole message is {}",phone,message);
            sb.append(phone + "#" + message);
            sb.append("\r");

            smsTaskDAO.insertShortLink(taskId, merchantId, phone, shortURL);

        }

        return sb.toString();
    }

    private List<String> filterPhoneList(List<String> phoneList, SMSTask task) {

        List<String> phonesToBeSend = new LinkedList<>();

        // 每个批次任务获取一次禁发区域
        List<ForbiddenDistrict> forbiddens = smsTaskDAO.qryForbiddenDistrict();

        for (String phone : phoneList) {

            Long taskId = task.getTaskId();
            Long merchantId = task.getMerchantId();

            //1.手机号码格式自验证
            if (!verifyUtil.isMobile(phone)) {
                logger.info("【Background Send】 phone is {}, 手机号码格式错误", phone);
                smsTaskDAO.insertUndeliverdPhone(taskId, merchantId, phone, "手机号码格式错误");
                continue;
            }

            //2.是否在黑名单，在的话插入发送失败表
            int black = smsSendDAO.qryPhoneInBlackList(phone);
            if (black > 0) {
                logger.info("【Background Send】 phone is {}, 手机号码在黑名单", phone);
                smsTaskDAO.insertUndeliverdPhone(taskId, merchantId, phone, "手机号码在黑名单");
                continue;
            }

            //3.去本地和百度查归属地，查到之后入库 取前7位
            String phonePrefix = phone.substring(0,7);
            logger.info("【Background Send】 phone prefix is {}", phonePrefix);
            PhoneDistrict district = smsTaskDAO.qryPhoneDistrict(phonePrefix);
            if (district == null) {
                // 找不到归属地 - 不能发
                logger.info("【Background Send】 phone is {}, 手机号码找不到归属地", phone);
                smsTaskDAO.insertUndeliverdPhone(taskId, merchantId, phone, "手机号码找不到归属地");
                continue;
            }


            //4.是否在禁发区域，在的话插入发送失败表
            boolean isForbidden = false;
            for (ForbiddenDistrict fb : forbiddens) {
                // 先看类型
                // logger.info("【Background Send】 phone is {}, dis.type is {}, fb.type is {}", phone, district.getType(), fb.getPhoneType());
                if (fb.getPhoneType().equals(district.getType())) {
                    // 比较省
                    // logger.info("【Background Send】 phone is {}, dis.prov is {}, fb.prov is {}", phone, district.getProv(), fb.getProvince());
                    if (fb.getProvince().equals(district.getProv())) {
                        // 比较市
                        // logger.info("【Background Send】 phone is {}, dis.city is {}, fb.city is {}", phone, district.getCity(), fb.getCity());
                        if ("ALL".equals(fb.getCity()) || fb.getCity().equals(district.getCity())) {
                            // 命中禁发区域
                            logger.info("【Background Send】 phone is {}, 手机号码属地{},{}禁发", phone, district.getProv(), district.getCity());
                            smsTaskDAO.insertUndeliverdPhone(taskId, merchantId, phone, "手机号码属地禁发");
                            isForbidden = true;
                            break;
                        }
                    }
                }
            }
            if (isForbidden) {
                continue;
            }
            logger.info("【Background Send】 ==========PHONE {} IS WIN==========", phone);
            phonesToBeSend.add(phone);
        }
        return phonesToBeSend;
    }

    private List<String> getPhoneListFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line = null;
            List<String> list = new LinkedList<>();
            while ((line = br.readLine()) != null) {
                if ("".equals(line) || line.trim().length() == 0) {
                    continue;
                }
                list.add(line);
            }
            return list;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
