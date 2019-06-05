package com.juhex.sms.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component(value = "smsSendDAO")
public class SMSSendDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer qryPhoneInBlackList(String phone){

        String sql = "SELECT COUNT(pid) total FROM blacklist WHERE mobile=?";
        Object[] paras = new Object[]{phone};
        Integer total = jdbcTemplate.queryForObject(sql,paras,Integer.TYPE);
        return total;

    }

    public Integer qryPhoneRegistered(String phone, Long merchantId) {
        String sql = "SELECT COUNT(req_id) total FROM mo_command WHERE merchant_id=? and mobile=?";
        Object[] paras = new Object[]{merchantId,phone};
        Integer total = jdbcTemplate.queryForObject(sql,paras,Integer.TYPE);
        return total;
    }

    public Integer qryPhoneSendTimes(String phone,Long merchantId){
        String sql = "SELECT COUNT(req_id) total FROM mt_vcode WHERE merchant_id=? and mobile=?";
        Object[] paras = new Object[]{merchantId,phone};
        Integer total = jdbcTemplate.queryForObject(sql,paras,Integer.TYPE);
        return total;
    }

    // 插入mt_vcode表
    // 注册验证码明细
    public void insertIntoMTVCode(Long merchantId,String msg, String mobile, String text, Long ticket){
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO mt_vcode (merchant_id,req_time,raw_msg,mobile,rs_text,ticket)");
        sql.append("values(?,NOW(),?,?,?,?)");

        Object[] paras = new Object[]{merchantId,msg,mobile,text,ticket};

        jdbcTemplate.update(sql.toString(),paras);
    }
}
