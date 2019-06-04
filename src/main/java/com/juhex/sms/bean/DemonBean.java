package com.juhex.sms.bean;

public class DemonBean {

    private Long calc = 0L;

    public DemonBean() {
        System.out.println("cons a demo bean");
    }

    public String doSomething(String para) {
        System.out.println("doSomething ".concat(para));
        calc = calc + 1L;
        return para.toLowerCase();
    }

    public Long getCalc(){
        return this.calc;
    }
}
