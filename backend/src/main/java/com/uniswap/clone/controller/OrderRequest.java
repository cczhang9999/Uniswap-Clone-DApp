package com.uniswap.clone.controller;

import com.uniswap.clone.model.Order;
import java.math.BigDecimal;

public class OrderRequest {
    private String userId;
    private String symbol;
    private Order.Side side;
    private Order.Type type;
    private BigDecimal price;
    private BigDecimal quantity;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Order.Side getSide() { return side; }
    public void setSide(Order.Side side) { this.side = side; }
    public Order.Type getType() { return type; }
    public void setType(Order.Type type) { this.type = type; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
