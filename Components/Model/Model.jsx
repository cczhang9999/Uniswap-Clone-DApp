import React, { useEffect, useState } from "react";
import Image from "next/image";

import Style from "./Model.module.css";
import images from "./../../assets";
const Model = ({ setOpenModel, connectWallet }) => {
  const walletMenu = ["Metamask", "Coinbase", "wallet", "walletConnect"];
  return (
    <div className={Style.model}>
      <div className={Style.model_box}>
        <div className={Style.model_box_heading}>
          <p>Connect a Wallet</p>
          <div className={Style.model_box_heading_img}>
            <Image
              src={images.close}
              alt="logo"
              width={50}
              height={50}
              onClick={() => setOpenModel(false)}
            />
          </div>
        </div>
        <div className={Style.model_box_wallet}>
          {walletMenu.map((el, i) => (
            <p key={i + 1} onClick={() => connectWallet()}>
              {el}
            </p>
          ))}
        </div>
        <p className={Style.model_box_para}>
          By connecting a wallet, you agree to Uniswap Lab's <br />
          Terms of service and privacy policy.
        </p>
      </div>
    </div>
  );
};

export default Model;
