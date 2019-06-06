package com.juhex.sms.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juhex.sms.bean.SMSResp;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 短信发送结果包解析器
 */
@Component(value = "smsRespPackageParser")
public class SMSRespPackageParser {


    private ObjectMapper om;

    public SMSRespPackageParser() {
        om = new ObjectMapper();
        // 忽略没有的属性
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public SMSResp parseSMSResultText(String json) {
        SMSResp resp = null;
        try {
            resp = om.readValue(json, SMSResp.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }

}
