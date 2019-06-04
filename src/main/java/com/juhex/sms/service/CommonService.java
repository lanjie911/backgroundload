package com.juhex.sms.service;

import com.juhex.sms.bean.DemonBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class CommonService {

    @Autowired
    private DemonBean bean;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void doService() {
        bean.doSomething("haha");
        Long lx = bean.getCalc();
        System.out.println("lx == "+lx);
    }

    public void doJDBCDemo(){
        jdbcTemplate.query("select * from merchant_admin",new Object[]{},(rs)->{
            while(rs.next()){
                System.out.println(rs.getString("admin_name"));
            }
        });
    }
}
