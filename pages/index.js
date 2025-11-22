import React, { useState, useContext, useEffect } from "react";
import { HeroSection } from "../Components/index";

const Home = () => {
  return (
    <div>
      <HeroSection accounts="hey" tokenData="DATA" />
    </div>
  );
};

export default Home;
