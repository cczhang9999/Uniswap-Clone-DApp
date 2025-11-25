const hre = require("hardhat");

// Hardcoded from constants.js to avoid ES6 import issues
const FACTORY_ADDRESS = "0x1F98431c8aD98523631AE4a59f267346ea31F984";

async function main() {
  const provider = hre.ethers.provider;
  const code = await provider.getCode(FACTORY_ADDRESS);
  
  if (code === "0x") {
    console.log("Uniswap V3 Factory NOT found at", FACTORY_ADDRESS);
    console.log("You are likely running a fresh Hardhat node without Mainnet forking.");
  } else {
    console.log("Uniswap V3 Factory found!");
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
