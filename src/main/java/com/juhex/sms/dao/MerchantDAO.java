package com.juhex.sms.dao;

import com.juhex.sms.bean.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MerchantDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Merchant queryByKeyCode(String keycode) {
        String sql = "SELECT * FROM merchant t WHERE t.keycode=?";
        Object[] paras = new Object[]{keycode};
        try {
            return jdbcTemplate.queryForObject(sql, paras, (rs, i) -> {
                Merchant m = new Merchant();
                m.setMerchantId(rs.getLong("merchant_id"));
                m.setMerchantName(rs.getString("merchant_name"));
                m.setDescription(rs.getString("description"));
                m.setMerchantStatus(rs.getInt("merchant_status"));
                m.setKeyCode(rs.getString("keycode"));
                m.setCreatedTime(rs.getString("created_time"));
                return m;
            });
        } catch (Exception e) {
            return null;
        }
    }

    public String qryTemplate(Long merchantId, Integer type) {
        String sql = "SELECT tempalte_text FROM sms_template WHERE merchant_id=? AND template_type=?";
        Object[] paras = new Object[]{merchantId, type};
        return jdbcTemplate.queryForObject(sql, paras, String.class);
    }

    public Integer qryShortURL(Long merchantId, String url) {
        String sql = "SELECT COUNT(1) total FROM short_link WHERE merchant_id=? AND s_url_link=?";
        Object[] paras = new Object[]{merchantId, url};
        return jdbcTemplate.queryForObject(sql, paras, Integer.TYPE);
    }

    public Integer deleteShortURL(Long merchantId, String mobile, String url) {
        String sql = "DELETE FROM short_link WHERE merchant_id=? AND phone_number=? AND s_url_link=?";
        Object[] paras = new Object[]{merchantId, mobile, url};
        int rs = 0;
        try {
            rs = jdbcTemplate.update(sql, paras);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public Integer deleteVCode(Long merchantId, String mobile, String vcode){
        String sql = "DELETE FROM vcode_verify WHERE merchant_id=? AND phone_number=? AND vcode=?";
        Object[] paras = new Object[]{merchantId, mobile, vcode};
        int rs = 0;
        try {
            rs = jdbcTemplate.update(sql, paras);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public void insertMOCommand(Long merchantId,String mobile,String rawMsg, String rawResp,Integer status){
        String sql = "INSERT INTO mo_command(merchant_id,mobile,raw_msg,raw_resp,rs_status,req_time)VALUES(?,?,?,?,?,NOW())";
        Object[] paras = new Object[]{merchantId, mobile, rawMsg,rawResp,status};
        try{
            jdbcTemplate.update(sql, paras);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
