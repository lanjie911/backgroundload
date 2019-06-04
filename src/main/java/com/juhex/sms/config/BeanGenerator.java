package com.juhex.sms.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.juhex.sms.bean.DemonBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class BeanGenerator {

    @Bean
    public DemonBean getDemonBean(){
        return new DemonBean();
    }

    @Bean(name={"dataSource"})
    @Qualifier(value = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource generateDataSource(){
        DataSource source = new DruidDataSource();
        return source;
    }

    // 这里将数据源的名字和JDBC模板进行了关联
    // 以后这里可以配置多数据源
    @Bean(name = "jdbcTemplate")
    public JdbcTemplate generateJdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
