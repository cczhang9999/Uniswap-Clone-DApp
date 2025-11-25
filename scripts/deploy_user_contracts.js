const hre = require("hardhat");

// Hardcoded Router address just in case constants.js import fails in script
const ROUTER_ADDRESS = "0xa82fF9aFd8f496c3d6ac40E2a0F282E47488CFc9";
const WETH_ADDRESS = "0x84eA74d481Ee0A5332c457a4d796187F6Ba67fEB"; // From previous deployment output

async function main() {
  const [owner] = await hre.ethers.getSigners();
  console.log("Deploying user contracts with account:", owner.address);
  console.log("Using Router Address:", ROUTER_ADDRESS);
  console.log("Using WETH Address:", WETH_ADDRESS);

  // Deploy SingleSwapToken
  const SingleSwapToken = await hre.ethers.getContractFactory("SingleSwapToken");
  const singleSwapToken = await SingleSwapToken.deploy(ROUTER_ADDRESS, WETH_ADDRESS);
  await singleSwapToken.deployed();
  console.log("SingleSwapToken deployed to:", singleSwapToken.address);

  // Deploy SwapMultiHop
  const SwapMultiHop = await hre.ethers.getContractFactory("SwapMultiHop");
  const swapMultiHop = await SwapMultiHop.deploy(ROUTER_ADDRESS, WETH_ADDRESS);
  await swapMultiHop.deployed();
  console.log("SwapMultiHop deployed to:", swapMultiHop.address);
  
  console.log("\nPLEASE UPDATE constants.js WITH THESE ADDRESSES:");
  console.log("SingleSwapTokenAddress:", singleSwapToken.address);
  console.log("SwapMultiHopAddress:", swapMultiHop.address);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
