package com.juhex.sms.scheduler;

import com.juhex.sms.dao.SMSTaskDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Deprecated
//@Component
//用来一次性导入数据用的类
//已经不用了
public class ExcelDataImport {

    private ExecutorService service;

    private Runnable run;

    private Logger logger;

    @Autowired
    private SMSTaskDAO smsTaskDAO;

    @PostConstruct
    public void init() {

        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

        service = Executors.newFixedThreadPool(1);

        run = () -> {
            try (BufferedReader br = new BufferedReader(new FileReader("E:/nodeproject/bel.csv"))) {

                String line = null;
                while ((line = br.readLine()) != null) {
                    String[] ts = line.split(",");
                    String mobile = ts[1];
                    String provn = ts[2];
                    String city = ts[3];
                    String type = ts[4];
                    logger.info("mobile is {}, provn is {}, city is {}, type is {}", mobile, provn, city, type);

                    smsTaskDAO.insertPhoneDistrict(mobile,provn,city,type);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        };

        service.submit(run);

    }
}
