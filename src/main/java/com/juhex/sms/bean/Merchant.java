package com.juhex.sms.bean;

public class Merchant {

    private Long merchantId;
    private String merchantName;
    private String createdTime;
    private String description;
    private Integer merchantStatus;
    private String keyCode;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMerchantStatus() {
        return merchantStatus;
    }

    public void setMerchantStatus(Integer merchantStatus) {
        this.merchantStatus = merchantStatus;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }
}
