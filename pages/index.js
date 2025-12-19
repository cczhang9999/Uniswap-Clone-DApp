import React, { useState, useContext, useEffect } from "react";
import { HeroSection, OrderBook, Wallet } from "../Components/index";

const Home = () => {
  const [userId, setUserId] = useState("user1");
  const [refreshCount, setRefreshCount] = useState(0);

  const triggerRefresh = () => setRefreshCount(prev => prev + 1);

  return (
    <div>
      <HeroSection accounts="hey" tokenData="DATA" />
      <Wallet 
        userId={userId} 
        setUserId={setUserId} 
        onSearch={triggerRefresh} // Pass trigger to Wallet
        refreshCount={refreshCount} // Pass count to Wallet
      />
      <OrderBook 
        userId={userId} 
        refreshCount={refreshCount} // Pass count to OrderBook
      />
    </div>
  );
};

export default Home;
