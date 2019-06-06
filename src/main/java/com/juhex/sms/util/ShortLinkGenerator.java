package com.juhex.sms.util;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;

@Component
public class ShortLinkGenerator {

    private String[] resourcePool;

    @PostConstruct
    public void init() {
        resourcePool = new String[62];
        List<String> pool = new ArrayList<>();
        for (int i = 48; i < 58; i++) {
            pool.add(String.valueOf((char) i));
        }
        for (int i = 65; i < 91; i++) {
            pool.add(String.valueOf((char) i));
        }
        for (int i = 97; i < 123; i++) {
            pool.add(String.valueOf((char) i));
        }
        resourcePool = pool.toArray(resourcePool);

    }

    private String compressData(String data) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DeflaterOutputStream zos = new DeflaterOutputStream(bos);
            zos.write(data.getBytes());
            zos.close();
            return new String(getenBASE64inCodec(bos.toByteArray()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ZIP_ERR";
        }
    }

    //  使用apche codec对数组进行encode
    private String getenBASE64inCodec(byte[] b) {
        if (b == null)
            return null;
        return new String((new Base64()).encode(b));
    }

    public String zipURL(String originURL) {

        String compString = this.compressData(originURL);

        int hashCode = Math.abs(compString.hashCode());

        String strHashCode = String.valueOf(hashCode);


        int len = strHashCode.length();
        int begin = 0;
        int end = 2;

        List<Integer> rList = new LinkedList<>();

        while (len >= 2) {
            String temp = strHashCode.substring(begin, end);
            int iTemp = Integer.parseInt(temp);
            rList.add(iTemp % 62);
            begin += 2;
            end += 2;
            len = len - 2;
        }

        int lenLimit = 1000;

        int i = 0;
        while (i < lenLimit) {
            Random r1 = new Random();
            int p1 = r1.nextInt(62);

            Random r2 = new Random();
            int p2 = r2.nextInt(62);

            String s1 = resourcePool[p1];
            String s2 = resourcePool[p2];

            resourcePool[p1] = s2;
            resourcePool[p2] = s1;
            i++;

        }
        StringBuilder sb = new StringBuilder();
        for(Integer pos : rList){
            sb.append(resourcePool[pos.intValue()]);
        }

        return sb.toString();

    }
}
