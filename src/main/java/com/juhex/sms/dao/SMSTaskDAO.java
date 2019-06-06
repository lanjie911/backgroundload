package com.juhex.sms.dao;

import com.juhex.sms.bean.ForbiddenDistrict;
import com.juhex.sms.bean.PhoneDistrict;
import com.juhex.sms.bean.SMSTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;

@Component(value = "smsTaskDAO")
public class SMSTaskDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public SMSTask qryOneSMSTask() {

        String sql = "SELECT * FROM sms_batch_task WHERE task_status=0 limit 1 offset 0";

        SMSTask taxk = null;

        try {
            taxk = jdbcTemplate.queryForObject(sql, (rs, i) -> {
                SMSTask task = new SMSTask();
                task.setTaskId(rs.getLong("task_id"));
                task.setMerchantId(rs.getLong("merchant_id"));
                task.setFilePath(rs.getString("file_path"));
                task.setStatus(rs.getInt("task_status"));
                return task;
            });
        }catch(EmptyResultDataAccessException e){
            taxk = null;
        }
        return taxk;
    }

    public void updateSMSTaskState(Long taskId){
        String sql = "UPDATE sms_batch_task SET task_status=1 WHERE task_id=? AND task_status=0";
        jdbcTemplate.update(sql,new Object[]{taskId});
    }

    public void insertUndeliverdPhone(Long taskId, Long merchantId, String phoneNumber, String desc) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO undeliverd_phone (task_id,merchant_id,phone_number,undeliverd_reason)");
        sql.append("values(?,?,?,?)");
        jdbcTemplate.update(sql.toString(), new Object[]{taskId, merchantId, phoneNumber, desc});
    }

    public PhoneDistrict qryPhoneDistrict(String phoneNumber) {
        String sql = "SELECT * FROM phone_district WHERE phone_number=?";

        PhoneDistrict disx = null;

        try {
            disx = jdbcTemplate.queryForObject(sql, new Object[]{phoneNumber}, (rs, i) -> {
                PhoneDistrict dis = new PhoneDistrict();
                dis.setPhone(phoneNumber);
                dis.setType(rs.getString("phone_type"));
                dis.setProv(rs.getString("province"));
                dis.setCity(rs.getString("city"));
                return dis;
            });
        }catch (EmptyResultDataAccessException e){
            //Get None Object
            disx = null;
        }

        return disx;
    }

    public void insertPhoneDistrict(String phoneNumber, String province, String city, String type) {
        String sql = "INSERT INTO phone_district (phone_number,province,city,phone_type)";
        sql += "values(?,?,?,?)";
        try {
            jdbcTemplate.update(sql, new Object[]{phoneNumber, province, city, type});
        }catch(DataAccessException e){
            e.printStackTrace();
        }
    }

    public List<ForbiddenDistrict> qryForbiddenDistrict() {
        String sql = "SELECT * FROM forbidden_district";
        List<ForbiddenDistrict> list =
                jdbcTemplate.query(sql, (rs, i) -> {
                    ForbiddenDistrict dis = new ForbiddenDistrict();
                    dis.setPrimaryId(rs.getLong("pid"));
                    dis.setPhoneType(rs.getString("phone_type"));
                    dis.setProvince(rs.getString("province"));
                    dis.setCity(rs.getString("city"));
                    return dis;
                });
        return list;
    }

    public void insertShortLink(Long taskId, Long merchantId,String phone,String url){
        String sql = "INSERT INTO short_link(task_id,merchant_id,phone_number,s_url_link)values(?,?,?,?)";
        jdbcTemplate.update(sql, new Object[]{taskId, merchantId, phone, url});
    }
}
