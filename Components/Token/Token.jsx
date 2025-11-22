import React, { useState, useEffect } from "react";
import Image from "next/image";

// import internal
import Style from "./Token.module.css";
import images from "./../../assets";
import { Toggle } from "../index";
const Token = (openSetting) => {
  return (
    <div className={Style.token}>
      <div className={Style.token_box}>
        <div className={Style.token_box_heading}>
          <h4>Setting</h4>
          <Image
            src={images.close}
            alt="close"
            width={50}
            height={50}
            onClick={() => openSetting.setOpenSetting(false)}
          />
        </div>
        <p className={Style.token_box_para}>
          Slippage tolerance {""}
          <Image src={images.lock} alt="img" width={20} height={20} />
        </p>
        <div className={Style.token_box_input}>
          <button>Auto</button>
          <input type="text" placeholder="0.10%" />
        </div>

        <p className={Style.token_box_para}>
          Slippage tolerance {""}
          <Image src={images.lock} alt="img" width={20} height={20} />
        </p>
        <div className={Style.token_box_input}>
          <input type="text" placeholder="30" />
          <button>minutes</button>
        </div>

        <h2>Interface Setting</h2>

        <div className={Style.token_box_toggle}>
          <p className={Style.token_box_para}>Transanction deadline</p>
          <Toggle label="No" />
        </div>
      </div>
    </div>
  );
};

export default Token;
