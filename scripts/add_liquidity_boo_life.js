const { ethers } = require("hardhat");

// Hardcoded addresses to avoid import issues
const NFT_MANAGER_ADDRESS = "0xf5059a5D33d5853360D16C683c16e67980206f36";
const BooTokenAddress = "0x95401dc811bb5740090279Ba06cfA8fcF6113778";
const LifeTokenAddress = "0x998abeb3E57409262aE5b751f60747921B33613E";

const artifacts = {
  NonfungiblePositionManager: require("@uniswap/v3-periphery/artifacts/contracts/NonfungiblePositionManager.sol/NonfungiblePositionManager.json"),
  BooToken: require("../artifacts/contracts/ERC20Boo.sol/BooToken.json"),
  LifeToken: require("../artifacts/contracts/ERC20Life.sol/LifeToken.json"),
};

async function main() {
  const [owner] = await ethers.getSigners();
  console.log("Adding liquidity for BooToken/LifeToken with account:", owner.address);

  const positionManager = new ethers.Contract(
    NFT_MANAGER_ADDRESS,
    artifacts.NonfungiblePositionManager.abi,
    owner
  );

  const booToken = new ethers.Contract(BooTokenAddress, artifacts.BooToken.abi, owner);
  const lifeToken = new ethers.Contract(LifeTokenAddress, artifacts.LifeToken.abi, owner);

  // Fee 3000 (0.3%)
  const fee = 3000;
  // Encode price sqrt(1/1) * 2^96
  const price = BigInt("79228162514264337593543950336"); 

  // Sort tokens
  const [token0, token1] = booToken.address.toLowerCase() < lifeToken.address.toLowerCase() 
    ? [booToken.address, lifeToken.address] 
    : [lifeToken.address, booToken.address];
  
  console.log(`Initializing pool for ${token0} and ${token1}`);

  await positionManager.createAndInitializePoolIfNecessary(
    token0,
    token1,
    fee,
    price,
    { gasLimit: 5000000 }
  );
  console.log("Pool initialized");

  // Approve PositionManager
  const amountToApprove = ethers.utils.parseEther("1000000");
  await booToken.approve(positionManager.address, amountToApprove);
  await lifeToken.approve(positionManager.address, amountToApprove);
  
  const params = {
    token0: token0,
    token1: token1,
    fee: fee,
    tickLower: -887220,
    tickUpper: 887220,
    amount0Desired: ethers.utils.parseEther("1000"),
    amount1Desired: ethers.utils.parseEther("1000"),
    amount0Min: 0,
    amount1Min: 0,
    recipient: owner.address,
    deadline: Math.floor(Date.now() / 1000) + 60 * 10,
  };

  const tx = await positionManager.mint(params, { gasLimit: 10000000 });
  await tx.wait();
  console.log("Liquidity added to BooToken/LifeToken pool");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
