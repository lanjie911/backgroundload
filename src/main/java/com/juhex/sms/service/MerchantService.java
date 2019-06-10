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

    public boolean isShortURLExist(Long merchantId, String url){
        Integer t = merchantDAO.qryShortURL(merchantId,url);
        return t > 0;
    }

    public boolean deleteShortURL(Long merchantId, String mobile, String url){
        Integer t = merchantDAO.deleteShortURL(merchantId,mobile,url);
        return t > 0;
    }

    public boolean deleteVCode(Long merchantId, String mobile, String vcode){
        Integer t = merchantDAO.deleteVCode(merchantId,mobile,vcode);
        return t > 0;
    }

    public void recordMOCommand(Long merchantId, String mobile, String rawMsg, String rawResp, Integer status){
        merchantDAO.insertMOCommand(merchantId,mobile,rawMsg,rawResp,status);
    }

}
