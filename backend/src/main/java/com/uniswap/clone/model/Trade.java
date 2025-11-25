package com.uniswap.clone.model;

import java.math.BigDecimal;

public class Trade {
    private String buyerUserId;
    private String sellerUserId;
    private String tradeId;
    private String symbol;
    private String buyOrderId;
    private String sellOrderId;
    private BigDecimal price;
    private BigDecimal quantity;
    private long timestamp;

    public Trade() {}

    public Trade(String tradeId, String buyerUserId, String sellerUserId, String symbol, String buyOrderId, String sellOrderId, BigDecimal price, BigDecimal quantity, long timestamp) {
        this.tradeId = tradeId;
        this.buyerUserId = buyerUserId;
        this.sellerUserId = sellerUserId;
        this.symbol = symbol;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getBuyerUserId() { return buyerUserId; }
    public void setBuyerUserId(String buyerUserId) { this.buyerUserId = buyerUserId; }
    public String getSellerUserId() { return sellerUserId; }
    public void setSellerUserId(String sellerUserId) { this.sellerUserId = sellerUserId; }

    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    public String getBuyOrderId() { return buyOrderId; }
    public void setBuyOrderId(String buyOrderId) { this.buyOrderId = buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
    public void setSellOrderId(String sellOrderId) { this.sellOrderId = sellOrderId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
