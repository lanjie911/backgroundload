package com.juhex.sms.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

// 发送单条短信
@Component(value = "smsClient")
public class SMSClient {


    @Autowired
    private SmsClientAccessTool smsClientAccessTool;

    public String sendSms(String url, String account, String password, String mobile, String content, String extno, String rt) {

        try {
            StringBuffer sendParam = new StringBuffer();
            sendParam.append("action=").append("send");
            sendParam.append("&account=").append(URLEncoder.encode(account, "UTF-8"));
            sendParam.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
            sendParam.append("&mobile=").append(URLEncoder.encode(mobile, "UTF-8"));
            sendParam.append("&content=").append(URLEncoder.encode(content, "UTF-8"));
            sendParam.append("&extno=").append(URLEncoder.encode(extno, "UTF-8"));
            sendParam.append("&rt=").append(URLEncoder.encode(rt, "UTF-8"));
            System.out.println(sendParam);

            return smsClientAccessTool.doAccessHTTPPost(url, sendParam.toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public String sendP2PSms(String url, String account, String password, String mobileContentList, String extno, String rt) {

        try {
            StringBuffer sendParam = new StringBuffer();
            sendParam.append("action=").append("p2p");
            sendParam.append("&account=").append(URLEncoder.encode(account, "UTF-8"));
            sendParam.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
            sendParam.append("&mobileContentList=").append(URLEncoder.encode(mobileContentList, "UTF-8"));
            sendParam.append("&extno=").append(URLEncoder.encode(extno, "UTF-8"));
            sendParam.append("&rt=").append(URLEncoder.encode(rt, "UTF-8"));
            System.out.println(sendParam);

            return smsClientAccessTool.doAccessHTTPPost(url, sendParam.toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public String queryReport(String url, String account, String password, String rt) {

        try {
            StringBuffer sendParam = new StringBuffer();
            sendParam.append("action=").append("report");
            sendParam.append("&account=").append(URLEncoder.encode(account, "UTF-8"));
            sendParam.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
            sendParam.append("&rt=").append(URLEncoder.encode(rt, "UTF-8"));

            return smsClientAccessTool.doAccessHTTPPost(url, sendParam.toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}
