import React from "react";
import styles from "./TokenList.module.css";
import Image from "next/image";
import images from "./../../assets";

const TokenList = ({ tokenDate, setOpenTokenBox }) => {
  const data = [1, 2, 3, 4, 5, 6, 7];
  return (
    <div className={styles.tokenList}>
      <p
        className={styles.tokenList_close}
        onClick={() => setOpenTokenBox(false)}
      >
        <Image src={images.close} alt="close" width={50} height={50} />
      </p>
      <div className={styles.tokenList_title}>
        <h2>Your Token List</h2>
      </div>
      {data.map((el, i) => (
        <div className={styles.tokenList_box}>
          <div className={styles.tokenList_box_info}>
            <p className={styles.tokenList_box_info_symbol}>HEY</p>
            <p>
              <span>34</span>GOLD COIN
            </p>
          </div>
        </div>
      ))}
    </div>
  );
};

export default TokenList;
