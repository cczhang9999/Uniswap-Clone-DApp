package com.uniswap.clone.engine;

import com.uniswap.clone.model.Order;
import com.uniswap.clone.model.Trade;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单簿 (OrderBook)
 * 核心撮合引擎逻辑，负责维护买单和卖单队列，并执行撮合。
 * 每个交易对 (Symbol) 对应一个 OrderBook 实例。
 */
public class OrderBook {
    private final String symbol;
    
    public String getSymbol() { return symbol; }
    
    // 买单队列 (Bids): 价格从高到低排序 (Price DESC)，价格相同时时间优先 (Time ASC)
    // 能够买入的最高价格排在最前面
    private final PriorityQueue<Order> bids;
    
    // 卖单队列 (Asks): 价格从低到高排序 (Price ASC)，价格相同时时间优先 (Time ASC)
    // 能够卖出的最低价格排在最前面
    private final PriorityQueue<Order> asks;

    public OrderBook(String symbol) {
        this.symbol = symbol;
        // 初始化买单队列
        this.bids = new PriorityQueue<>((o1, o2) -> {
            int priceCompare = o2.getPrice().compareTo(o1.getPrice()); // 降序：高价优先
            if (priceCompare != 0) return priceCompare;
            return Long.compare(o1.getTimestamp(), o2.getTimestamp()); // 升序：早时间优先
        });
        // 初始化卖单队列
        this.asks = new PriorityQueue<>((o1, o2) -> {
            int priceCompare = o1.getPrice().compareTo(o2.getPrice()); // 升序：低价优先
            if (priceCompare != 0) return priceCompare;
            return Long.compare(o1.getTimestamp(), o2.getTimestamp()); // 升序：早时间优先
        });
    }

    /**
     * 处理新订单
     * @param order 新进入的订单
     * @return 撮合生成的交易列表
     */
    public List<Trade> processOrder(Order order) {
        List<Trade> trades = new ArrayList<>();
        // 如果是买单，尝试与卖单队列 (Asks) 撮合
        if (order.getSide() == Order.Side.BUY) {
            matchOrder(order, asks, trades);
        } else {
            // 如果是卖单，尝试与买单队列 (Bids) 撮合
            matchOrder(order, bids, trades);
        }
        
        // 如果撮合后订单仍有剩余数量，且为限价单 (LIMIT)，则加入订单簿等待后续撮合
        // (市价单 MARKET 如果未完全成交通常会撤销或转为限价，此处简化处理：仅限价单入队)
        if (order.getQuantity().compareTo(BigDecimal.ZERO) > 0 && order.getType() == Order.Type.LIMIT) {
            addOrder(order);
        }
        return trades;
    }

    /**
     * 撮合逻辑
     * @param incomingOrder 新进入的订单
     * @param oppositeBook 对手方订单队列 (买单 vs 卖单队列 / 卖单 vs 买单队列)
     * @param trades 存放生成的交易记录
     */
    private void matchOrder(Order incomingOrder, PriorityQueue<Order> oppositeBook, List<Trade> trades) {
        // 当对手方队列不为空，且新订单还有剩余数量时，循环尝试撮合
        while (!oppositeBook.isEmpty() && incomingOrder.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            Order bestMatch = oppositeBook.peek(); // 获取对手方最优价格订单
            
            // 检查价格条件是否满足
            if (incomingOrder.getSide() == Order.Side.BUY) {
                // 买单：如果买入价 < 卖一价，无法成交，退出循环
                if (incomingOrder.getType() == Order.Type.LIMIT && incomingOrder.getPrice().compareTo(bestMatch.getPrice()) < 0) {
                    System.out.println("Price not match for BUY: " + incomingOrder.getPrice() + " < " + bestMatch.getPrice());
                    break; 
                }
            } else {
                // 卖单：如果卖出价 > 买一价，无法成交，退出循环
                if (incomingOrder.getType() == Order.Type.LIMIT && incomingOrder.getPrice().compareTo(bestMatch.getPrice()) > 0) {
                    break; 
                }
            }

            // 执行成交
            // 成交数量 = min(新订单剩余量, 对手单剩余量)
            BigDecimal matchQuantity = incomingOrder.getQuantity().min(bestMatch.getQuantity());
            // 成交价格 = 对手单价格 (Maker Price) - 遵循“价格优先”原则，以挂单价格成交
            BigDecimal matchPrice = bestMatch.getPrice(); 

            // 生成交易记录
            Trade trade = new Trade(
                    UUID.randomUUID().toString(),
                    incomingOrder.getSide() == Order.Side.BUY ? incomingOrder.getUserId() : bestMatch.getUserId(),
                    incomingOrder.getSide() == Order.Side.SELL ? incomingOrder.getUserId() : bestMatch.getUserId(),
                    symbol,
                    incomingOrder.getSide() == Order.Side.BUY ? incomingOrder.getOrderId() : bestMatch.getOrderId(),
                    incomingOrder.getSide() == Order.Side.SELL ? incomingOrder.getOrderId() : bestMatch.getOrderId(),
                    matchPrice,
                    matchQuantity,
                    System.currentTimeMillis()
            );
            trades.add(trade);

            // 更新订单剩余数量
            incomingOrder.setQuantity(incomingOrder.getQuantity().subtract(matchQuantity));
            bestMatch.setQuantity(bestMatch.getQuantity().subtract(matchQuantity));

            // 如果对手单已完全成交，从队列中移除
            if (bestMatch.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                oppositeBook.poll(); 
            }
        }
    }

    /**
     * 将未完全成交的订单加入订单簿
     */
    private void addOrder(Order order) {
        if (order.getSide() == Order.Side.BUY) {
            bids.add(order);
        } else {
            asks.add(order);
        }
    }
    
    // 获取买单深度快照 (用于 API 展示)
    public List<Order> getBids() {
        List<Order> list = new ArrayList<>(bids);
        list.sort(bids.comparator()); // 重新排序以确保列表顺序正确
        return list;
    }
    
    // 获取卖单深度快照 (用于 API 展示)
    public List<Order> getAsks() {
        List<Order> list = new ArrayList<>(asks);
        list.sort(asks.comparator()); // 重新排序以确保列表顺序正确
        return list;
    }
}
