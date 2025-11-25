const hre = require("hardhat");
const { Contract } = require("ethers");
const { artifacts } = require("hardhat");

// Addresses from previous deployment (or constants.js)
// NOTE: You might need to update these if you redeploy!
const BOO_TOKEN_ADDRESS = "0xA51c1fc2f0D1a1b8494Ed1FE312d7C3a78Ed91C0";
const LIFE_TOKEN_ADDRESS = "0x0DCd1Bf9A1b36cE34237eEaFef220932846BCD82";
// WETH address on Localhost (Hardhat default WETH9 or deployed one)
// Usually Hardhat doesn't have WETH pre-deployed at the mainnet address.
// We might need to deploy a Mock WETH or use the one if SingleSwapToken deployed it?
// Wait, SingleSwapToken usually relies on SwapRouter which relies on WETH9.
// Let's check if we have a WETH contract or if we need to deploy one.
// For now, let's assume we need to interact with the NonfungiblePositionManager to create a pool.

const NONFUNGIBLE_POSITION_MANAGER_ADDRESS = "0xC36442b4a4522E871399CD717aBDD847Ab11FE88";
const WETH_ADDRESS = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"; // Mainnet WETH, likely wrong for localhost unless forked

async function main() {
  const [owner] = await hre.ethers.getSigners();
  console.log("Initializing pool with account:", owner.address);

  // 1. Get Contract Instances
  const BooToken = await hre.ethers.getContractAt("BooToken", BOO_TOKEN_ADDRESS);
  const LifeToken = await hre.ethers.getContractAt("LifeToken", LIFE_TOKEN_ADDRESS);
  
  // We need the Position Manager to create pool and add liquidity
  // Note: On a local hardhat node without forking, these addresses (0xC364...) won't exist!
  // If we are NOT forking mainnet, we need to deploy Uniswap V3 Core + Periphery first.
  // If we ARE forking (which hardhat.config.js seemed to imply with 'forking' disabled but maybe user enabled it?), 
  // then we can use mainnet addresses.
  
  // Let's check if we can access the Position Manager.
  // If not, we simply cannot swap on localhost without deploying the entire Uniswap V3 suite.
  
  // HOWEVER, the user's `SingleSwapToken` contract imports `ISwapRouter`.
  // If `SingleSwapToken` was deployed successfully, it means it linked to *something*.
  // But `deploy.js` didn't deploy Router/Factory.
  
  // CRITICAL: If running on empty localhost, we MUST deploy Uniswap V3 contracts or mock them.
  // Or, maybe the user IS forking mainnet?
  // Let's try to verify if WETH exists.
  
  const weth = await hre.ethers.getContractAt("IWETH", WETH_ADDRESS);
  try {
      const name = await weth.name();
      console.log("WETH Name:", name);
  } catch (e) {
      console.log("WETH not found at", WETH_ADDRESS);
      console.log("Are you running with Mainnet Forking?");
      // If WETH is not found, we can't really proceed with standard Uniswap V3 swap 
      // unless we deploy our own WETH and Router.
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
