package com.juhex.sms.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component(value = "smsSendDAO")
public class SMSSendDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer qryPhoneInBlackList(String phone) {

        String sql = "SELECT COUNT(pid) total FROM blacklist WHERE mobile=?";
        Object[] paras = new Object[]{phone};
        Integer total = jdbcTemplate.queryForObject(sql, paras, Integer.TYPE);
        return total;

    }

    public Integer qryPhoneRegistered(String phone, Long merchantId) {
        String sql = "SELECT COUNT(req_id) total FROM mo_command WHERE merchant_id=? and mobile=?";
        Object[] paras = new Object[]{merchantId, phone};
        Integer total = jdbcTemplate.queryForObject(sql, paras, Integer.TYPE);
        return total;
    }

    public Integer qryPhoneSendTimes(String phone, Long merchantId) {
        String sql = "SELECT COUNT(req_id) total FROM mt_vcode WHERE merchant_id=? and mobile=?";
        Object[] paras = new Object[]{merchantId, phone};
        Integer total = jdbcTemplate.queryForObject(sql, paras, Integer.TYPE);
        return total;
    }

    // 插入mt_vcode表
    // 注册验证码明细
    public void insertMTVCode(Long merchantId, String msg, Integer status, String mobile, String mid, Integer result) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO mt_vcode (merchant_id,req_time,raw_msg,rs_status,mobile,mid,rs_result)");
        sql.append("values(?,NOW(),?,?,?,?,?)");
        Object[] paras = new Object[]{merchantId, msg, status, mobile, mid, result};
        try {
            jdbcTemplate.update(sql.toString(), paras);
        }catch (Exception e){
            e.printStackTrace();
            //continue
        }
    }

    public void insertMTCommand(Long merchantId, String msg, Integer status, String mobile, String mid, Integer result) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO mt_command (merchant_id,req_time,raw_msg,rs_status,mobile,mid,rs_result)");
        sql.append("values(?,NOW(),?,?,?,?,?)");
        Object[] paras = new Object[]{merchantId, msg, status, mobile, mid, result};
        try {
            jdbcTemplate.update(sql.toString(), paras);
        }catch (Exception e){
            e.printStackTrace();
            //continue
        }
    }

    public void insertVCodeVerify(Long merchantId , String mobile , String vcode){
        String sql = "INSERT INTO vcode_verify ";
        sql += "(merchant_id,phone_number,vcode)VALUES";
        sql += "(?,?,?)";
        Object[] paras = new Object[]{merchantId, mobile, vcode};
        try {
            jdbcTemplate.update(sql, paras);
        }catch (Exception e){
            e.printStackTrace();
            //continue
        }
    }

    // 更新验证码发送状态
    public void updateMTVCode(String mobile, String mid, String state) {
        String sql = "UPDATE mt_vcode SET rs_stat=? WHERE mobile=? and mid=?";
        Object[] paras = new Object[]{state, mobile, mid};
        try{
            jdbcTemplate.update(sql, paras);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void updateMTCommand(String mobile, String mid, String state) {
        String sql = "UPDATE mt_command SET rs_stat=? WHERE mobile=? and mid=?";
        Object[] paras = new Object[]{state, mobile, mid};
        try{
            jdbcTemplate.update(sql, paras);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
