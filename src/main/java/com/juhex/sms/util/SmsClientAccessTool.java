package com.juhex.sms.util;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@Component
public class SmsClientAccessTool {


    /**
     * <p>
     * POST方法
     * </p>
     *
     * @param sendUrl        ：访问URL
     * @param sendParam      ：参数串
     * @param backEncodeType ：返回的编码
     * @return 返回是否发送成功
     */
    public String doAccessHTTPPost(String sendUrl, String sendParam, String backEncodeType) {
        StringBuffer receive = new StringBuffer();

        if (backEncodeType == null || backEncodeType.equals("")) {
            backEncodeType = "UTF-8";
        }


        try {
            URL url = new URL(sendUrl);
            HttpURLConnection URLConn = (HttpURLConnection) url.openConnection();
            URLConn.setDoOutput(true);
            URLConn.setDoInput(true);
            URLConn.setRequestMethod("POST");
            URLConn.setUseCaches(false);
            URLConn.setAllowUserInteraction(true);
            HttpURLConnection.setFollowRedirects(true);
            URLConn.setInstanceFollowRedirects(true);

            URLConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            URLConn.setRequestProperty("Content-Length", String.valueOf(sendParam.getBytes().length));

            DataOutputStream dos = new DataOutputStream(URLConn.getOutputStream());
            dos.writeBytes(sendParam);

            BufferedReader rd = new BufferedReader(new InputStreamReader(URLConn.getInputStream(), backEncodeType));
            String line;
            while ((line = rd.readLine()) != null) {
                receive.append(line).append("\r\n");
            }
            rd.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            receive.append("URL ERROR");
        } catch (ProtocolException e) {
            e.printStackTrace();
            receive.append("Protocol ERROR");
        } catch (IOException e) {
            e.printStackTrace();
            receive.append("IO ERROR");
        }

        return receive.toString();
    }

    public String doAccessHTTPGet(String sendUrl, String backEncodeType) {

        StringBuffer receive = new StringBuffer();

        if (backEncodeType == null || backEncodeType.equals("")) {
            backEncodeType = "UTF-8";
        }

        try {
            URL url = new URL(sendUrl);
            HttpURLConnection URLConn = (HttpURLConnection) url.openConnection();
            URLConn.setDoInput(true);
            URLConn.setDoOutput(true);
            URLConn.connect();
            URLConn.getOutputStream().flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(URLConn.getInputStream(), backEncodeType));

            String line;
            while ((line = in.readLine()) != null) {
                receive.append(line).append("\r\n");
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            receive.append("IO ERROR");
        }

        return receive.toString();
    }
}
