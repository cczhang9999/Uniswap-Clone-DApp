package com.uniswap.clone.model;

import java.math.BigDecimal;

public class Balance {
    private String currency;
    private BigDecimal available;
    private BigDecimal frozen;

    public Balance() {}

    public Balance(String currency, BigDecimal available, BigDecimal frozen) {
        this.currency = currency;
        this.available = available;
        this.frozen = frozen;
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAvailable() { return available; }
    public void setAvailable(BigDecimal available) { this.available = available; }
    public BigDecimal getFrozen() { return frozen; }
    public void setFrozen(BigDecimal frozen) { this.frozen = frozen; }
}
