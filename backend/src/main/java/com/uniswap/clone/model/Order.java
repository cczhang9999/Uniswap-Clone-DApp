package com.uniswap.clone.model;

import java.math.BigDecimal;

public class Order {
    private String userId;
    private String orderId;
    private String symbol;
    private Side side;
    private Type type;
    private BigDecimal price;
    private BigDecimal quantity;
    private long timestamp;

    public Order() {}

    public Order(String userId, String orderId, String symbol, Side side, Type type, BigDecimal price, BigDecimal quantity, long timestamp) {
        this.userId = userId;
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Side getSide() { return side; }
    public void setSide(Side side) { this.side = side; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public enum Side { BUY, SELL }
    public enum Type { LIMIT, MARKET }
}
