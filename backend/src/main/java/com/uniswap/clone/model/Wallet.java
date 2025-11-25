package com.uniswap.clone.model;

import java.util.HashMap;
import java.util.Map;

public class Wallet {
    private String userId;
    private Map<String, Balance> balances;

    public Wallet() {
        this.balances = new HashMap<>();
    }

    public Wallet(String userId) {
        this.userId = userId;
        this.balances = new HashMap<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Map<String, Balance> getBalances() { return balances; }
    public void setBalances(Map<String, Balance> balances) { this.balances = balances; }
}
