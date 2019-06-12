package com.juhex.sms.dao;

import com.juhex.sms.mocker.PhoneDelivered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockerDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<PhoneDelivered> qryUnreported(String tableName){
        String sql = "SELECT mobile, mid FROM "+tableName+" WHERE rs_stat IS NULL limit 10 offset 0";
        try{
            return jdbcTemplate.query(sql,(rs,i)->{
                PhoneDelivered pd = new PhoneDelivered();
                pd.setMid(rs.getString("mid"));
                pd.setPhoneNumber(rs.getString("mobile"));
                return pd;
            });
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
