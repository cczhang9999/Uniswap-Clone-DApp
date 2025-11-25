import React, { useContext } from "react";
import Image from "next/image";
import { SwapContext } from "../Context/SwapContext";
import images from "../assets";
import Style from "../Components/Token/Token.module.css";

const Pools = () => {
  const { topTokenList } = useContext(SwapContext);

  // Mock pool data - in a real app, this would come from smart contracts
  const mockPools = [
    {
      token0: topTokenList[0],
      token1: topTokenList[1],
      fee: "0.3%",
      tvl: "$1,234.56",
      volume24h: "$567.89",
    },
    {
      token0: topTokenList[1],
      token1: topTokenList[2],
      fee: "0.3%",
      tvl: "$2,345.67",
      volume24h: "$890.12",
    },
  ];

  return (
    <div className={Style.pools}>
      <div className={Style.pools_box}>
        <h2 className={Style.pools_heading}>Liquidity Pools</h2>
        
        <div className={Style.pools_list}>
          {mockPools.map((pool, index) => (
            <div key={index} className={Style.pools_item}>
              <div className={Style.pools_item_tokens}>
                <div className={Style.pools_item_token_pair}>
                  <Image
                    src={pool.token0?.image || images.etherlogo}
                    alt={pool.token0?.name}
                    width={30}
                    height={30}
                  />
                  <Image
                    src={pool.token1?.image || images.etherlogo}
                    alt={pool.token1?.name}
                    width={30}
                    height={30}
                    className={Style.pools_item_token_overlap}
                  />
                </div>
                <div className={Style.pools_item_info}>
                  <p className={Style.pools_item_name}>
                    {pool.token0?.symbol} / {pool.token1?.symbol}
                  </p>
                  <small className={Style.pools_item_fee}>Fee: {pool.fee}</small>
                </div>
              </div>
              <div className={Style.pools_item_stats}>
                <div className={Style.pools_item_stat}>
                  <small>TVL</small>
                  <p>{pool.tvl}</p>
                </div>
                <div className={Style.pools_item_stat}>
                  <small>24h Volume</small>
                  <p>{pool.volume24h}</p>
                </div>
              </div>
            </div>
          ))}
        </div>

        <p className={Style.pools_note}>
          Note: Pool data is currently mocked. Connect to actual Uniswap V3 pools for real data.
        </p>
      </div>
    </div>
  );
};

export default Pools;
