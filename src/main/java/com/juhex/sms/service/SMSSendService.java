package com.juhex.sms.service;

import com.juhex.sms.dao.SMSSendDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SMSSendService {

    @Autowired
    private SMSSendDAO smsSendDAO;

    public boolean isSMSCouldBeDelivered(){
        return false;
    }
}
