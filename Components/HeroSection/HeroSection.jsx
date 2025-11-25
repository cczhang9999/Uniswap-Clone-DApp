import React, { useState, useContext } from "react";
import Image from "next/image";

import Style from "./HeroSection.module.css";
import images from "../../assets";
import { Token, SearchToken } from "../index";

//CONTEXT
import { SwapContext } from "../../Context/SwapContext";

const HeroSection = ({}) => {
  const [openSetting, setOpenSetting] = useState(false);
  const [openToken, setOpenToken] = useState(false);
  const [openTokensTwo, setOpenTokensTwo] = useState(false);

  const { account, weth9, dai, ether, connectWallet, tokenData, isLoading, singleSwapToken, topTokenList } =
    useContext(SwapContext);

  //token 1
  const [tokenOne, setTokenOne] = useState({
    name: "",
    image: "",
  });
  //token 2
  const [tokenTwo, setTokenTwo] = useState({
    name: "",
    image: "",
  });
  
  const [enteredValue, setEnteredValue] = useState("");

  React.useEffect(() => {
    if (topTokenList) {
      setTokenOne(topTokenList[0]);
      setTokenTwo(topTokenList[1]);
    }
  }, [topTokenList]);

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
              onClick={() => setOpenSetting(true)}
            />
          </div>
        </div>

        <div className={Style.HeroSection_box_input}>
          <input 
            type="text" 
            placeholder="0" 
            value={enteredValue}
            onChange={(e) => setEnteredValue(e.target.value)}
          />
          <button onClick={() => setOpenToken(true)}>
            <Image
              src={tokenOne.image || images.etherlogo}
              width={20}
              height={20}
              alt="ether"
            />
            {tokenOne.name || "ETH"}
            <small>{tokenOne.tokenBalance || "0"}</small>
          </button>
        </div>

        <div className={Style.HeroSection_box_input}>
          <input type="text" placeholder="0" />
          <button onClick={() => setOpenTokensTwo(true)}>
            <Image
              src={tokenTwo.image || images.etherlogo}
              width={20}
              height={20}
              alt="ether"
            />
            {tokenTwo.name || "ETH"}
            <small>{tokenTwo.tokenBalance || "0"}</small>
          </button>
        </div>

        {account ? (
          <button
            className={Style.HeroSection_box_btn}
            onClick={() =>
              singleSwapToken({
                token1: tokenOne,
                token2: tokenTwo,
                swapAmount: enteredValue,
              })
            }
          >
            {isLoading ? "Loading..." : "Swap"}
          </button>
        ) : (
          <button
            onClick={() => connectWallet()}
            className={Style.HeroSection_box_btn}
          >
            Connect Wallet
          </button>
        )}
      </div>

      {openSetting && <Token setOpenSetting={setOpenSetting} />}

      {openToken && (
        <SearchToken
          openToken={setOpenToken}
          tokens={setTokenOne}
          tokenData={tokenData.length > 0 ? tokenData : topTokenList}
        />
      )}
      {openTokensTwo && (
        <SearchToken
          openToken={setOpenTokensTwo}
          tokens={setTokenTwo}
          tokenData={tokenData.length > 0 ? tokenData : topTokenList}
        />
      )}
    </div>
  );
};

export default HeroSection;
