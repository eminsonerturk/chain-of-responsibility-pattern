package com.example.ChainOfResponsibilityPattern.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class LimitDTO {
    private String customerNo;
    private BigDecimal dailyLimit;
    private BigDecimal usedToday;
    private String currency;

    public String getCustomerNo() {
        return customerNo;
    }

    public void setCustomerNo(String customerNo) {
        this.customerNo = customerNo;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getUsedToday() {
        return usedToday;
    }

    public void setUsedToday(BigDecimal usedToday) {
        this.usedToday = usedToday;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}