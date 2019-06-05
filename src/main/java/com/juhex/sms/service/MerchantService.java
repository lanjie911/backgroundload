package com.juhex.sms.service;

import com.juhex.sms.bean.Merchant;
import com.juhex.sms.dao.MerchantDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    @Autowired
    private MerchantDAO merchantDAO;

    public Merchant getMerchantByKeycode(String keycode) {
        return merchantDAO.queryByKeyCode(keycode);
    }

}
