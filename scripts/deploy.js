const hre = require("hardhat");

async function main() {
  // Deploy BooToken
  const BooToken = await hre.ethers.getContractFactory("BooToken");
  const booToken = await BooToken.deploy();
  await booToken.deployed();
  console.log(`BooToken deployed to ${booToken.address}`);

  // Deploy LifeToken
  const LifeToken = await hre.ethers.getContractFactory("LifeToken");
  const lifeToken = await LifeToken.deploy();
  await lifeToken.deployed();
  console.log(`LifeToken deployed to ${lifeToken.address}`);

  // Deploy SingleSwapToken
  const SingleSwapToken = await hre.ethers.getContractFactory("SingleSwapToken");
  const singleSwapToken = await SingleSwapToken.deploy();
  await singleSwapToken.deployed();
  console.log(`SingleSwapToken deployed to ${singleSwapToken.address}`);

  // Deploy SwapMultiHop
  const SwapMultiHop = await hre.ethers.getContractFactory("SwapMultiHop");
  const swapMultiHop = await SwapMultiHop.deploy();
  await swapMultiHop.deployed();
  console.log(`SwapMultiHop deployed to ${swapMultiHop.address}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
