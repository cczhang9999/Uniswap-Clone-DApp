import React, { useContext, useEffect, useState } from "react";
import Image from "next/image";
import Link from "next/link";

import Style from "./NavBar.module.css";

import images from "../../assets";
import Model from "../Model/Model";
import TokenList from "../index";
import { SwapContext } from "../../Context/SwapContext";

const NavBar = () => {
  const { account, connectWallet, networkConnect } = useContext(SwapContext);
  const { topTokenList, tokenData } = useContext(SwapContext);
  const [search, setSearch] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [showResults, setShowResults] = useState(false);
  
  const menuItems = [
    {
      name: "Swap",
      link: "/",
    },
    {
      name: "Tokens",
      link: "/Tokens",
    },
    {
      name: "Pools",
      link: "/Pools",
    },
  ];

  const [openModel, setOpenModel] = useState(false);
  const [openTokenBox, setOpenTokenBox] = useState(false);

  const handleSearch = (e) => {
    const query = e.target.value;
    setSearch(query);
    
    if (query.trim() === "") {
      setSearchResults([]);
      setShowResults(false);
      return;
    }

    const allTokens = [...topTokenList, ...tokenData];
    const filtered = allTokens.filter(
      (token) =>
        token.name?.toLowerCase().includes(query.toLowerCase()) ||
        token.symbol?.toLowerCase().includes(query.toLowerCase()) ||
        token.tokenAddress?.toLowerCase().includes(query.toLowerCase())
    );
    
    setSearchResults(filtered);
    setShowResults(true);
  };

  return (
    <div className={Style.navbar}>
      <div className={Style.navbar_box}>
        <div className={Style.navbar_box_left}>
          {/* logo imgage*/}
          <div className={Style.navbar_box_left_img}>
            <Image src={images.uniswap} alt="logo" width={50} height={50} />
          </div>
          {/* Menu items */}
          <div className={Style.navbar_box_left_menu}>
            {menuItems.map((el, i) => (
              <Link
                key={i + 1}
                href={el.link}
              >
                {/* Menu items */}
                <p className={Style.navbar_box_left_menu_item}>{el.name}</p>
              </Link>
            ))}
          </div>
        </div>
        {/* middle section */}
        <div className={Style.navbar_box_middle}>
          <div className={Style.navbar_box_middle_search}>
            <div className={Style.navbar_box_middle_search_img}>
              <Image src={images.search} alt="search" width={20} height={20} />
            </div>
            {/* input section */}
            <input 
              type="text" 
              placeholder="Search Tokens" 
              value={search}
              onChange={handleSearch}
              onFocus={() => search && setShowResults(true)}
              onBlur={() => setTimeout(() => setShowResults(false), 200)}
            />
            {/* Search results dropdown */}
            {showResults && searchResults.length > 0 && (
              <div className={Style.navbar_search_results}>
                {searchResults.slice(0, 5).map((token, index) => (
                  <div 
                    key={index} 
                    className={Style.navbar_search_item}
                    onClick={() => {
                      setSearch("");
                      setShowResults(false);
                    }}
                  >
                    <Image
                      src={token.image || images.etherlogo}
                      alt={token.name}
                      width={20}
                      height={20}
                    />
                    <div className={Style.navbar_search_item_info}>
                      <p>{token.name}</p>
                      <small>{token.symbol}</small>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
        {/* right section */}
        <div className={Style.navbar_box_right}>
          <div className={Style.navbar_box_right_box}>
            <div className={Style.navbar_box_right_box_img}>
              <Image src={images.ether} alt="NetWork" width={30} height={30} />
            </div>
            <p>{networkConnect}</p>
          </div>
          {account ? (
            <button onClick={() => setOpenModel(true)}>
              {account.slice(0, 5) + "..." + account.slice(38, 42)}
            </button>
          ) : (
            <button onClick={() => connectWallet()}>Connect</button>
          )}

          {openModel && (
            <Model setOpenModel={setOpenModel} connectWallet={connectWallet} />
          )}
        </div>
      </div>
      {openTokenBox && (
        <TokenList tokenDate="hey" setOpenTokenBox={setOpenTokenBox} />
      )}
    </div>
  );
};

export default NavBar;
