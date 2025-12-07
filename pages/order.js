
import React, { useState, useEffect } from "react";
import { OrderBook, Wallet } from "../Components/index";

const Order = () => {
  const [userId, setUserId] = useState("user1");

  return (
    <div>
      <Wallet userId={userId} setUserId={setUserId} />
      <OrderBook userId={userId} />
    </div>
  );
};

export default Order;
