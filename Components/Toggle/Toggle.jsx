import React from "react";
import styles from "./Toggle.module.css";
const Toggle = ({ label }) => {
  return (
    <div className={styles.toggle}>
      <div className={styles.toggle_switch_box}>
        <input
          type="checkbox"
          className={styles.toggle_switch_checkbox}
          name={label}
          id={label}
        />
        <label className={styles.toggle_switch_label} htmlFor={label}>
          <span className={styles.toggle_switch_inner}></span>
          <span className={styles.toggle_switch_switch}></span>
        </label>
      </div>
    </div>
  );
};

export default Toggle;
