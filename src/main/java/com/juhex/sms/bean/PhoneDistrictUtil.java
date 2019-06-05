package com.juhex.sms.bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PhoneDistrictUtil {

    private HttpClient client;

    private final String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?cb=jQuery1102011288117839578748_{timestamp}&resource_name=guishudi&query={phoneNumber}&_={timestamp}";

    private Logger logger;

    private ObjectMapper mapper;

    public PhoneDistrictUtil(){
        client = HttpClientBuilder.create().build();
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
        mapper = new ObjectMapper();
    }

    public PhoneDistrict fetchPhoneDistrictByBaiduAPI(String phoneNumber) {

        long timeStamp = System.currentTimeMillis();
        String reqURL = url.replaceAll("\\{timestamp\\}",timeStamp+"");
        reqURL = reqURL.replaceAll("\\{phoneNumber\\}",phoneNumber);
        logger.info("Fetch mobile phone district, the url is : {}",reqURL);

        HttpGet getReq = new HttpGet(reqURL);

        RequestConfig requestConfig = RequestConfig.custom()
                // 设置连接到对方服务器的超时时间
                .setConnectTimeout(2000)
                // 设置从连接池拿到连接的超时时间和数据库连接池类似
                .setConnectionRequestTimeout(1000)
                // 设置数据传输超时时间，如果数据太大，则超时放弃
                .setSocketTimeout(3000).build();

        getReq.setConfig(requestConfig);

        try {
            HttpResponse resp = client.execute(getReq);

            int statusCode = resp.getStatusLine().getStatusCode();
            if(statusCode != 200) {
                // http code 不是200
                logger.info("Fetch mobile phone district failed, http response status code is : {}",statusCode);
                return null;
            }

            String responseText = EntityUtils.toString(resp.getEntity(), "UTF-8");
            logger.info("Fetch mobile phone district response text is : {}",responseText);

            int beginIndex = responseText.indexOf("(")+1;
            int endIndex = responseText.lastIndexOf(")");
            String jsonText = responseText.substring(beginIndex,endIndex);
            logger.info("Fetch mobile phone district filtered text is : {}",jsonText);

            beginIndex = jsonText.indexOf("[{")+1;
            endIndex = jsonText.lastIndexOf("}]")+1;
            String jsonData = jsonText.substring(beginIndex,endIndex);

            logger.info("JSON data is : {}",jsonData);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            PhoneDistrict pd = mapper.readValue(jsonData,PhoneDistrict.class);

            return pd;
        } catch (IOException e) {
            logger.warn(e.getMessage(),e);
        }
        return null;
    }

}
