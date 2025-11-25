import React, { useContext, useState } from "react";
import Image from "next/image";
import { SwapContext } from "../Context/SwapContext";
import images from "../assets";
import Style from "../Components/Token/Token.module.css";

const Tokens = () => {
  const { topTokenList, tokenData } = useContext(SwapContext);
  const [search, setSearch] = useState("");

  // Combine all tokens
  const allTokens = [...topTokenList, ...tokenData];
  
  // Filter tokens based on search
  const filteredTokens = allTokens.filter(
    (token) =>
      token.name?.toLowerCase().includes(search.toLowerCase()) ||
      token.symbol?.toLowerCase().includes(search.toLowerCase()) ||
      token.tokenAddress?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className={Style.tokens}>
      <div className={Style.tokens_box}>
        <h2 className={Style.tokens_heading}>Token List</h2>
        
        <div className={Style.tokens_search}>
          <Image src={images.search} alt="search" width={20} height={20} />
          <input
            type="text"
            placeholder="Search by name, symbol or address"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className={Style.tokens_list}>
          {filteredTokens.length > 0 ? (
            filteredTokens.map((token, index) => (
              <div key={index} className={Style.tokens_item}>
                <div className={Style.tokens_item_left}>
                  <Image
                    src={token.image || images.etherlogo}
                    alt={token.name}
                    width={40}
                    height={40}
                  />
                  <div className={Style.tokens_item_info}>
                    <p className={Style.tokens_item_name}>{token.name}</p>
                    <small className={Style.tokens_item_symbol}>{token.symbol}</small>
                  </div>
                </div>
                <div className={Style.tokens_item_right}>
                  <p className={Style.tokens_item_balance}>{token.tokenBalance || "0.00"}</p>
                  <small className={Style.tokens_item_address}>
                    {token.tokenAddress?.slice(0, 6)}...{token.tokenAddress?.slice(-4)}
                  </small>
                </div>
              </div>
            ))
          ) : (
            <p className={Style.tokens_empty}>No tokens found</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Tokens;
