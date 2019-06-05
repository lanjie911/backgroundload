package com.juhex.sms.bean;

import java.util.LinkedList;
import java.util.List;

public class SMSResp {

    //"{\"status\":\"0\",\"balance\":6720,\"list\":[{\"mid\":\"65412C03827FD020\",\"mobile\":\"13810613412\",\"result\":0}]}";


    private Integer status;
    private Integer balance;
    private List<SMSPackage> list;

    public SMSResp(){
        list = new LinkedList<>();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public List<SMSPackage> getList() {
        return list;
    }

    public void setList(List<SMSPackage> list) {
        this.list = list;
    }
}
