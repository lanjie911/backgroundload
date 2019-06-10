package com.juhex.sms.controller;

import com.juhex.sms.bean.Merchant;
import com.juhex.sms.scheduler.BackgroundJKDHttpSender;
import com.juhex.sms.service.MerchantService;
import com.juhex.sms.util.HttpJSONResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class RegisterFacade {

    private Logger logger;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private BackgroundJKDHttpSender backgroundJKDHttpSender;

    public RegisterFacade() {
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }

    private String domain = "loan.juhedx.com";
    private String port = "";
    private String protocol = "http";
    private String WHOLE_URL;
    private HttpJSONResponseWriter<Map> writer = new HttpJSONResponseWriter<>();

    @PostConstruct
    public void init() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) {
            port = ":8080";
            domain = "127.0.0.1";
        }
        WHOLE_URL = protocol + "://" + domain + port + "/jkd/index.html";
    }


    @RequestMapping(method = {RequestMethod.GET}, path = {"/t/{shortURL}"})
    public void verifyCode(@PathVariable String shortURL, HttpServletRequest req, HttpServletResponse resp) {
        logger.info("Register URL is {}", shortURL);

        // 验证短连接是否存在
        String wholeURl = protocol + "://" + domain + "/t/" + shortURL;
        logger.info("whole url is {}", wholeURl);
        boolean isExist = merchantService.isShortURLExist(1L, wholeURl);
        if (!isExist) {
            resp.setStatus(404);
            try {
                resp.getWriter().write("Page Not Found!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        String redirectURL = WHOLE_URL + "?q=" + shortURL;
        // 根据 URL 跳转到对应的商户注册页面
        logger.info("redirect to {}", redirectURL);

        try {
            resp.setStatus(302);
            resp.sendRedirect(redirectURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(method = {RequestMethod.GET}, path = {"/jkd/reg"})
    @ResponseBody
    public String regJKD(@RequestParam(name = "q") String mobile, @RequestParam(name = "keycode") String keyCode, @RequestParam(name = "vcode") String vcode, @RequestParam("ctype") String ctype,@RequestParam(name = "surl") String shortURL) {

        // 查询商户是否存在
        Merchant merchant = merchantService.getMerchantByKeycode(keyCode);
        Map resp = new HashMap<String, String>();
        if (merchant == null) {
            resp.put("rs", "-1");
            resp.put("text", "数据主体错误");
            return writer.generate(resp);
        }

        // 删除验证码是否OK
        boolean isOK = merchantService.deleteVCode(1L, mobile, vcode);
        if (!isOK) {
            resp.put("rs", "-1");
            resp.put("text", "验证码错误");
            return writer.generate(resp);
        }

        // 删除短连接是否OK
        String wholeURl = protocol + "://" + domain + "/t/" + shortURL;
        logger.info("whole url is {}", wholeURl);
        isOK = merchantService.deleteShortURL(1L, mobile, wholeURl);
        if (!isOK) {
            resp.put("rs", "-1");
            resp.put("text", "网络地址错误");
            return writer.generate(resp);
        }


        backgroundJKDHttpSender.submitTask(merchant.getMerchantId(),mobile,ctype);

        resp.put("rs", "0");
        resp.put("text", "注册成功");
        return writer.generate(resp);
    }
}
