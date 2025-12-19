import React, { useState, useEffect } from "react";
import Style from "./OrderBook.module.css";

const OrderBook = ({ userId, refreshCount }) => {
  const API_URL = process.env.NEXT_PUBLIC_API_URL;
  const [symbol, setSymbol] = useState("BTC-USDT");
  const [orderType, setOrderType] = useState("LIMIT");
  const [side, setSide] = useState("BUY");
  const [price, setPrice] = useState("");
  const [quantity, setQuantity] = useState("");
  const [bids, setBids] = useState([]);
  const [asks, setAsks] = useState([]);

  // Fetch Order Book Data
  // 获取订单簿数据 (Fetch Order Book Data)
  useEffect(() => {
    const fetchOrderBook = async () => {
      try {
        // 调用后端 API 获取指定交易对 (symbol) 的订单簿数据
        const response = await fetch(`${API_URL}/api/v1/order/book/${symbol}`);
        const result = await response.json();
        if (result.code === 200) {
          const data = result.data;
          // 更新买单 (Bids) 和卖单 (Asks) 状态
          setBids(data.bids || []);
          setAsks(data.asks || []);
        } else {
          console.error("Failed to fetch order book:", result.message);
        }
      } catch (error) {
        console.error("Failed to fetch order book:", error);
      }
    };

    fetchOrderBook(); // 组件加载时立即获取一次
    //const interval = setInterval(fetchOrderBook, 2000); // 设置定时器，每 1 秒轮询一次最新数据
    //return () => clearInterval(interval); // 组件卸载时清除定时器，防止内存泄漏
  }, [symbol]); // 依赖项为 symbol，当 symbol 变化时重新执行

  // Place Order
  const placeOrder = async () => {
    if (!userId) {
      alert("Please enter a User ID in the Wallet section first.");
      return;
    }
    try {
      const response = await fetch(`${API_URL}/api/v1/order`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userId,
          symbol,
          side,
          type: orderType,
          price: parseFloat(price),
          quantity: parseFloat(quantity),
        }),
      });

      const result = await response.json();

      if (result.code === 200) {
        alert("Order Placed Successfully!");
        setPrice("");
        setQuantity("");
      } else {
        alert(`Failed to place order: ${result.message}`);
      }
    } catch (error) {
      console.error("Error placing order:", error);
      alert("Error placing order");
    }
  };

  const [myOrders, setMyOrders] = useState([]);
  const [trades, setTrades] = useState([]);

  // Fetch My Orders
  useEffect(() => {
    if (!userId) return;
    
    const fetchMyOrders = async () => {
      try {
        const response = await fetch(`${API_URL}/api/v1/orders/${userId}`);
        const result = await response.json();
        if (result.code === 200) {
          setMyOrders(result.data);
        }
      } catch (error) {
        console.error("Failed to fetch my orders:", error);
      }
    };

    const fetchTrades = async () => {
      try {
        const response = await fetch(`${API_URL}/api/v1/trades/${userId}`);
        const result = await response.json();
        if (result.code === 200) {
          setTrades(result.data);
        }
      } catch (error) {
        console.error("Failed to fetch trades:", error);
      }
    };

    fetchMyOrders();
    fetchTrades();
    //const interval = setInterval(fetchMyOrders, 2000);
    //return () => clearInterval(interval);
  }, [userId, refreshCount]);

  return (
    <div className={Style.OrderBook}>
      <div className={Style.OrderBook_box}>
        {/* Left Side: Order Form */}
        <div className={Style.OrderBook_box_left}>
          <div className={Style.OrderBook_box_heading}>
            <p>Trade {symbol}</p>
          </div>
          
          <div className={Style.OrderBook_box_input}>
            <select 
              value={side} 
              onChange={(e) => setSide(e.target.value)}
              style={{ width: "100%", padding: "1rem", marginBottom: "1rem", backgroundColor: "#2c2f36", color: "white", border: "none", borderRadius: "0.5rem" }}
            >
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
          </div>

          <div className={Style.OrderBook_box_input}>
            <input
              type="number"
              placeholder="Price"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
            />
          </div>
          
          <div className={Style.OrderBook_box_input}>
            <input
              type="number"
              placeholder="Quantity"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
            />
          </div>

          <button className={Style.OrderBook_box_btn} onClick={placeOrder}>
            {side} {symbol}
          </button>
        </div>

        {/* Right Side: Order Book */}
        <div className={Style.OrderBook_box_right}>
          <div className={Style.OrderBook_list_header}>
            <span>Price</span>
            <span>Amount</span>
          </div>
          
          {/* Asks (Sells) - Red */}
          <div className={Style.OrderBook_list} style={{ flexDirection: "column-reverse" }}>
            {asks.map((ask, i) => (
              <div key={i} className={`${Style.OrderBook_list_item} ${Style.OrderBook_list_item_sell}`}>
                <span>{ask.price}</span>
                <span>{ask.quantity}</span>
              </div>
            ))}
          </div>

          <div style={{ margin: "1rem 0", borderTop: "1px solid #333" }}></div>

          {/* Bids (Buys) - Green */}
          <div className={Style.OrderBook_list}>
            {bids.map((bid, i) => (
              <div key={i} className={`${Style.OrderBook_list_item} ${Style.OrderBook_list_item_buy}`}>
                <span>{bid.price}</span>
                <span>{bid.quantity}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
      
      {/* My Orders Section */}
      <div className={Style.OrderBook_box} style={{ marginTop: "2rem", display: "block" }}>
        <div className={Style.OrderBook_box_heading}>
          <p>My Orders</p>
        </div>
        <table style={{ width: "100%", color: "#fff", textAlign: "left", borderCollapse: "collapse" }}>
            <thead>
                <tr style={{ borderBottom: "1px solid #333" }}>
                    <th style={{ padding: "10px" }}>Time</th>
                    <th style={{ padding: "10px" }}>Symbol</th>
                    <th style={{ padding: "10px" }}>Side</th>
                    <th style={{ padding: "10px" }}>Type</th>
                    <th style={{ padding: "10px" }}>Price</th>
                    <th style={{ padding: "10px" }}>Qty</th>
                    <th style={{ padding: "10px" }}>Status</th>
                </tr>
            </thead>
            <tbody>
                {myOrders.length === 0 ? (
                    <tr><td colSpan="7" style={{ padding: "20px", textAlign: "center", color: "#888" }}>No orders found</td></tr>
                ) : (
                    myOrders.map((order) => (
                        <tr key={order.orderId} style={{ borderBottom: "1px solid #222" }}>
                            <td style={{ padding: "10px" }}>{new Date(order.timestamp).toLocaleTimeString()}</td>
                            <td style={{ padding: "10px" }}>{order.symbol}</td>
                            <td style={{ padding: "10px", color: order.side === 'BUY' ? '#00ff00' : '#ff0000' }}>{order.side}</td>
                            <td style={{ padding: "10px" }}>{order.type}</td>
                            <td style={{ padding: "10px" }}>{order.price}</td>
                            <td style={{ padding: "10px" }}>{order.quantity}</td>
                            <td style={{ padding: "10px" }}>{order.status}</td>
                        </tr>
                    ))
                )}
            </tbody>
        </table>
      </div>

      {/* Trade History Section */}
      <div className={Style.OrderBook_box} style={{ marginTop: "2rem", display: "block" }}>
        <div className={Style.OrderBook_box_heading}>
          <p>Trade History</p>
        </div>
        <table style={{ width: "100%", color: "#fff", textAlign: "left", borderCollapse: "collapse" }}>
            <thead>
                <tr style={{ borderBottom: "1px solid #333" }}>
                    <th style={{ padding: "10px" }}>Time</th>
                    <th style={{ padding: "10px" }}>Symbol</th>
                    <th style={{ padding: "10px" }}>Side</th>
                    <th style={{ padding: "10px" }}>Price</th>
                    <th style={{ padding: "10px" }}>Qty</th>
                    <th style={{ padding: "10px" }}>Role</th>
                </tr>
            </thead>
            <tbody>
                {trades.length === 0 ? (
                    <tr><td colSpan="6" style={{ padding: "20px", textAlign: "center", color: "#888" }}>No trades found</td></tr>
                ) : (
                    trades.map((trade) => {
                        const isBuyer = trade.buyerUserId === userId;
                        const role = isBuyer ? "Buyer" : "Seller";
                        const sideColor = isBuyer ? "#00ff00" : "#ff0000"; // Green for Buy, Red for Sell
                        return (
                            <tr key={trade.tradeId} style={{ borderBottom: "1px solid #222" }}>
                                <td style={{ padding: "10px" }}>{new Date(trade.timestamp).toLocaleTimeString()}</td>
                                <td style={{ padding: "10px" }}>{trade.symbol}</td>
                                <td style={{ padding: "10px", color: sideColor }}>{isBuyer ? 'BUY' : 'SELL'}</td>
                                <td style={{ padding: "10px" }}>{trade.price}</td>
                                <td style={{ padding: "10px" }}>{trade.quantity}</td>
                                <td style={{ padding: "10px" }}>{role}</td>
                            </tr>
                        );
                    })
                )}
            </tbody>
        </table>
      </div>
    </div>
  );
};

export default OrderBook;
