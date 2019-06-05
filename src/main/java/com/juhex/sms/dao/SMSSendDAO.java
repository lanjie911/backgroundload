package com.juhex.sms.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component(value = "smsSendDAO")
public class SMSSendDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;
}
