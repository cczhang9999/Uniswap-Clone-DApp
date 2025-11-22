import React, { useContext, useEffect, useState } from "react";
import Image from "next/image";

import Style from "./HeroSection.module.css";
import images from "../../assets";
import { Token, SearchToken } from "../index";
const HeroSection = ({ accounts, tokenData }) => {
  const [openSetting, setOpenSetting] = useState(false);
  const [openToken, setOpenToken] = useState(false);
  const [openTokensTwo, setOpenTokensTwo] = useState(false);

  //token 1
  const [token1, setToken1] = useState({
    name: "",
    images: "",
  });
  //token 2
  const [token2, setToken2] = useState({
    name: "",
    images: "",
  });
  //jsx
  return (
    <div className={Style.HeroSection}>
      <div className={Style.HeroSection_box}>
        <div className={Style.HeroSection_box_heading}>
          <p>Swap</p>
          <div className={Style.HeroSection_box_heading_img}>
            <Image
              src={images.close}
              alt="image"
              width={50}
              height={50}
              onClink={() => setOpenSetting(true)}
            />
          </div>
        </div>
        <div className={Style.HeroSection_box_input}>
          <input type="text" placeholder="0.0" />
          <button onClick={() => openToken(true)}>
            <Image
              src={images.image || images.etherlogo}
              alt="ether"
              width={20}
              height={20}
            />
            {token1.name || "ETH"}
            <small>9474</small>
          </button>
        </div>
        <div className={Style.HeroSection_box_input}>
          <input type="text" placeholder="0.0" />
          <button onClick={() => openToken(true)}>
            <Image
              src={token2.image || images.etherlogo}
              alt="ether"
              width={20}
              height={20}
            />
            {token2.name || "ETH"}
            <small>9474</small>
          </button>
        </div>
        {accounts ? (
          <button className={Style.HeroSection_box_btn}>Connect Wallet</button>
        ) : (
          <button
            className={Style.HeroSection_box_btn}
            onClick={() => {
              222();
            }}
          >
            Swap
          </button>
        )}
      </div>
      {!openSetting && <Token setOpenSetting={setOpenSetting} />}
      {openToken && (
        <SearchToken
          openToken={setOpenToken}
          tokens={setToken1}
          tokenData={tokenData}
        />
      )}
      {openToken && (
        <SearchToken
          openToken={setOpenTokensTwo}
          tokens={setToken2}
          tokenData={tokenData}
        />
      )}
    </div>
  );
};

export default HeroSection;
