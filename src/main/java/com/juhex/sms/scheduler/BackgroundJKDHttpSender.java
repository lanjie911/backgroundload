package com.juhex.sms.scheduler;

import com.juhex.sms.service.MerchantService;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BackgroundJKDHttpSender {

    private ExecutorService es;

    private Logger logger;

    @Autowired
    private MerchantService merchantService;

    @PostConstruct
    private void init(){
        es = Executors.newFixedThreadPool(5);
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }

    private final void handleHTTPReq(Long merchantId,String mobile , String ctype){
        try{
            // 都没问题发送请求
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost postReq = new HttpPost("http://jkd.ryxfintech.com/api.php/External/Marketing/ChannelRegister");

            String jsonString = "{\"Mobile\":\""+mobile+"\",\"client\":\""+ctype+"\",\"puser\":\"wytg\",\"ppass\":\"1234qwer\"}";
            String jsonStringEnc = "{\"Mobile\":\""+mobile+"\",\"client\":\""+ctype+"\",\"puser\":\"****\",\"ppass\":\"********\"}";

            StringEntity entity = new StringEntity(jsonString, "UTF-8");
            postReq.setEntity(entity);
            postReq.setHeader("Content-Type", "application/json;charset=utf8");

            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接到对方服务器的超时时间
                    .setConnectTimeout(2000)
                    // 设置从连接池拿到连接的超时时间和数据库连接池类似
                    .setConnectionRequestTimeout(1000)
                    // 设置数据传输超时时间，如果数据太大，则超时放弃
                    .setSocketTimeout(3000).build();

            postReq.setConfig(requestConfig);

            CloseableHttpResponse response = null;
            String respJson = "";
            int respStatus = 0;
            try {
                // 由客户端执行(发送)Post请求
                response = client.execute(postReq);
                // 从响应模型中获取响应实体
                HttpEntity responseEntity = response.getEntity();
                respStatus = response.getStatusLine().getStatusCode();
                logger.info("[JKD HTTP Response Status is ] : {}",respStatus);

                if (responseEntity != null) {
                    respJson = EntityUtils.toString(responseEntity);
                    logger.info("[JKD HTTP Response Content ] : {}, {}",mobile,respJson);
                }
                client.close();

                // 报文入库
                merchantService.recordMOCommand(merchantId,mobile,jsonStringEnc,respJson,respStatus);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void submitTask(Long merchantId, String mobile,String ctype){
        Runnable task = ()->{
            handleHTTPReq(merchantId,mobile,ctype);
        };

        this.es.submit(task);
    }
}
