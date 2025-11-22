import React, { useContext, useEffect, useState } from "react";
import Image from "next/image";
import Link from "next/link";

import Style from "./NavBar.module.css";

import images from "../../assets";
import Model from "../Model/Model";
import TokenList from "../index";
const NavBar = () => {
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
                href={{ pathname: `${el.name}`, query: `${el.link}` }}
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
            <input type="text" placeholder="Search Tokens" />
          </div>
        </div>
        {/* right section */}
        <div className={Style.navbar_box_right}>
          <div className={Style.navbar_box_right_box}>
            <div className={Style.navbar_box_right_box_img}>
              <Image src={images.ether} alt="NetWork" width={30} height={30} />
            </div>
            <p>Network Name</p>
          </div>
          <button onClick={() => setOpenModel(true)}>Address</button>
          {openModel && (
            <Model setOpenModel={setOpenModel} connectWallet="Connect" />
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
