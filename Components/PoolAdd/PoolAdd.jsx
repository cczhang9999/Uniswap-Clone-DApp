import React, { useState, useEffect, useContext } from "react";
import Image from "next/image";

import images from "../../assets";
import Style from "./PoolAdd.module.css";
import { Token, SearchToken } from "../../Components/index";
import { SwapContext } from "../../Context/SwapContext";

const PoolAdd = () => {
  const { account, connectWallet, isLoading } = useContext(SwapContext);
  const [openModel, setOpenModel] = useState(false);
  const [openTokenModel, setOpenTokenModel] = useState(false);
  const [active, setActive] = useState(1);
  const [openTokenModelTwo, setOpenTokenModelTwo] = useState(false);

  const [tokenOne, setTokenOne] = useState({
    name: "",
    image: "",
  });

  const [tokenTwo, setTokenTwo] = useState({
    name: "",
    image: "",
  });

  return (
    <div className={Style.PoolAdd}>
      <div className={Style.PoolAdd_box}>
        <div className={Style.PoolAdd_box_header}>
          <div className={Style.PoolAdd_box_header_left}>
            <Image src={images.arrowLeft} alt="image" width={30} height={30} />
          </div>
          <div className={Style.PoolAdd_box_header_middle}>
            <p>Add Liquidity</p>
          </div>
          <div className={Style.PoolAdd_box_header_right}>
            <Image
              src={images.close}
              alt="image"
              width={50}
              height={50}
              onClick={() => setOpenModel(true)}
            />
          </div>
        </div>

        {/* SELECT TOKEN SECTION */}
        <div className={Style.PoolAdd_box_price}>
          {/* LEFT */}
          <div className={Style.PoolAdd_box_price_left}>
            <div className={Style.PoolAdd_box_price_left_token_input}>
              <div className={Style.PoolAdd_box_price_left_token_input_img}>
                <Image
                  src={tokenOne.image || images.etherlogo}
                  alt="image"
                  width={20}
                  height={20}
                />
              </div>
              <input type="text" placeholder="0" />
            </div>
            <div
              className={Style.PoolAdd_box_price_left_token_info}
              onClick={() => setOpenTokenModel(true)}
            >
              <p>{tokenOne.name || "ETH"}</p>
            </div>
          </div>
          {/* RIGHT */}
          <div className={Style.PoolAdd_box_price_right}>
            <div className={Style.PoolAdd_box_price_right_token_input}>
              <div className={Style.PoolAdd_box_price_right_token_input_img}>
                <Image
                  src={tokenTwo.image || images.etherlogo}
                  alt="image"
                  width={20}
                  height={20}
                />
              </div>
              <input type="text" placeholder="0" />
            </div>
            <div
              className={Style.PoolAdd_box_price_right_token_info}
              onClick={() => setOpenTokenModelTwo(true)}
            >
              <p>{tokenTwo.name || "ETH"}</p>
            </div>
          </div>
        </div>

        {/* FEE SECTION */}
        <div className={Style.PoolAdd_box_deposit}>
          <h4>Fee tier</h4>
          <div className={Style.PoolAdd_box_deposit_box}>
            {[1, 2, 3].map((el, i) => (
              <div
                className={`${Style.PoolAdd_box_deposit_box_item} ${
                  active == i + 1 ? Style.active : ""
                }`}
                key={i + 1}
                onClick={() => setActive(i + 1)}
              >
                <p>0.05%</p>
                <small>Best for Stable pairs</small>
              </div>
            ))}
          </div>
        </div>

        {/* BUTTON */}
        <div className={Style.PoolAdd_box_btn}>
          {account ? (
            <button>{isLoading ? "Loading..." : "Add Liquidity"}</button>
          ) : (
            <button onClick={() => connectWallet()}>Connect Wallet</button>
          )}
        </div>
      </div>

      {openModel && (
        <div className={Style.token_overlay}>
          <Token setOpenSetting={setOpenModel} />
        </div>
      )}
      {openTokenModel && (
        <div className={Style.token_overlay}>
          <SearchToken
            openToken={setOpenTokenModel}
            tokens={setTokenOne}
            tokenData={[]}
          />
        </div>
      )}
      {openTokenModelTwo && (
        <div className={Style.token_overlay}>
          <SearchToken
            openToken={setOpenTokenModelTwo}
            tokens={setTokenTwo}
            tokenData={[]}
          />
        </div>
      )}
    </div>
  );
};

export default PoolAdd;
