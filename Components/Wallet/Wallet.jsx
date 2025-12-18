import React, { useState, useEffect } from "react";
import Style from "./Wallet.module.css";

const Wallet = ({ userId, setUserId }) => {
  const API_URL = process.env.NEXT_PUBLIC_API_URL;
  const [balances, setBalances] = useState({});
  const [depositCurrency, setDepositCurrency] = useState("USDT");
  const [depositAmount, setDepositAmount] = useState("");

  // Fetch Balance
  const fetchBalance = async () => {
    if (!userId) return;
    try {
      const response = await fetch(`${API_URL}/api/v1/balance/${userId}`);
      const result = await response.json();
      if (result.code === 200) {
        setBalances(result.data.balances || {});
      } else {
        console.error("Failed to fetch balance:", result.message);
      }
    } catch (error) {
      console.error("Failed to fetch balance:", error);
    }
  };

  useEffect(() => {
    fetchBalance();
    //const interval = setInterval(fetchBalance, 2000);
    //return () => clearInterval(interval);
  }, [userId]);

  // Deposit Funds
  const handleDeposit = async () => {
    try {
      const response = await fetch(`${API_URL}/api/v1/balance/deposit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId,
          currency: depositCurrency,
          amount: parseFloat(depositAmount),
        }),
      });

      const result = await response.json();

      if (result.code === 200) {
        alert("Deposit Successful!");
        setDepositAmount("");
        fetchBalance();
      } else {
        alert(`Deposit Failed: ${result.message}`);
      }
    } catch (error) {
      console.error("Deposit error:", error);
      alert("Deposit Error");
    }
  };

  return (
    <div className={Style.Wallet}>
      <div className={Style.Wallet_box}>
        <div className={Style.Wallet_box_heading}>
          <p>Wallet Management</p>
        </div>

        {/* User ID Input */}
        <div className={Style.Wallet_box_input}>
          <label>Current User ID (Simulated Login)</label>
          <input
            type="text"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            placeholder="Enter User ID (e.g. user1)"
          />
        </div>

        {/* Balance Display */}
        <div className={Style.Wallet_balance_list}>
          {["BTC", "USDT"].map((currency) => {
            const bal = balances[currency] || { available: 0, frozen: 0 };
            return (
              <div key={currency} className={Style.Wallet_balance_item}>
                <h4>{currency}</h4>
                <p>Available: {bal.available}</p>
                <p>Frozen: {bal.frozen}</p>
              </div>
            );
          })}
        </div>

        {/* Deposit Form */}
        <div style={{ borderTop: "1px solid #333", paddingTop: "1rem" }}>
          <p style={{ marginBottom: "1rem", fontWeight: "bold" }}>Deposit Funds</p>
          <div className={Style.Wallet_box_input}>
            <select
              value={depositCurrency}
              onChange={(e) => setDepositCurrency(e.target.value)}
            >
              <option value="USDT">USDT</option>
              <option value="BTC">BTC</option>
              <option value="ETH">ETH</option>
            </select>
          </div>
          <div className={Style.Wallet_box_input}>
            <input
              type="number"
              placeholder="Amount"
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
            />
          </div>
          <button className={Style.Wallet_box_btn} onClick={handleDeposit}>
            Deposit
          </button>
        </div>
      </div>
    </div>
  );
};

export default Wallet;
