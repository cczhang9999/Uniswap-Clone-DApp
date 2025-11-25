import React, { useState, useContext, useEffect } from "react";
import { HeroSection, OrderBook, Wallet } from "../Components/index";

const Home = () => {
  const [userId, setUserId] = useState("user1");

  return (
    <div>
      <HeroSection accounts="hey" tokenData="DATA" />
      <Wallet userId={userId} setUserId={setUserId} />
      <OrderBook userId={userId} />
    </div>
  );
};

export default Home;
