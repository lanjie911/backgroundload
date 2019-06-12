package com.juhex.sms.mocker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juhex.sms.bean.SMSPackage;
import com.juhex.sms.bean.SMSResp;
import com.juhex.sms.dao.MockerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

@Component
public class DevEnvSMSSenderMocker {

    private ObjectMapper om;

    @Autowired
    private MockerDAO mockerDAO;

    public DevEnvSMSSenderMocker(){
        om = new ObjectMapper();
    }

    private SMSResp generateResp(){
        SMSResp resp = new SMSResp();
        resp.setStatus(0);
        resp.setBalance(9999);
        List<SMSPackage> packages = new LinkedList<>();
        resp.setList(packages);
        return resp;
    }

    private String writeJSON(SMSResp resp){
        StringWriter sw = new StringWriter();
        try {
            om.writeValue(sw,resp);
        } catch (IOException e) {
            e.printStackTrace();
            sw.write("ERROR");
        }
        return sw.toString();
    }

    public String mockSMSResp(String mobile){
        SMSResp resp = this.generateResp();
        List<SMSPackage> packages = resp.getList();

        SMSPackage pack = new SMSPackage();
        pack.setMid(String.valueOf(System.currentTimeMillis()));
        pack.setMobile(mobile);
        pack.setResult(0);
        pack.setStat("0");
        packages.add(pack);

        return this.writeJSON(resp);
    }

    public String mockSMSResp(String[] phones){
        SMSResp resp = this.generateResp();
        List<SMSPackage> packages = resp.getList();

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

        return this.writeJSON(resp);
    }

    private String handleReport(List<PhoneDelivered> list){
        SMSResp resp = this.generateResp();
        List<SMSPackage> packages = resp.getList();
        for(PhoneDelivered pd:list){
            SMSPackage pack = new SMSPackage();
            pack.setMid(pd.getMid());
            pack.setStat("DELIVERED");
            pack.setMobile(pd.getPhoneNumber());
            pack.setResult(200);
            packages.add(pack);
        }
        return this.writeJSON(resp);
    }

    public String mockSMSReport(){
        List<PhoneDelivered> list = mockerDAO.qryUnreported("mt_vcode");
        return this.handleReport(list);
    }

    public String mockP2PReport(){
        List<PhoneDelivered> list = mockerDAO.qryUnreported("mt_command");
        return this.handleReport(list);
    }
}
