package com.juhex.sms.dao;

import com.juhex.sms.bean.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MerchantDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Merchant queryByKeyCode(String keycode){
        String sql = "SELECT * FROM merchant t WHERE t.keycode=?";
        Object[] paras = new Object[]{keycode};
        return jdbcTemplate.queryForObject(sql,paras,(rs,i)->{
            Merchant m = new Merchant();
            m.setMerchantId(rs.getLong("merchant_id"));
            m.setMerchantName(rs.getString("merchant_name"));
            m.setDescription(rs.getString("description"));
            m.setMerchantStatus(rs.getInt("merchant_status"));
            m.setKeyCode(rs.getString("keycode"));
            m.setCreatedTime(rs.getString("created_time"));
            return m;
        });
    }
}
