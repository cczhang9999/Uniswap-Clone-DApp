const { ethers } = require("hardhat");
const artifacts = {
  UniswapV3Factory: require("@uniswap/v3-core/artifacts/contracts/UniswapV3Factory.sol/UniswapV3Factory.json"),
  SwapRouter: require("@uniswap/v3-periphery/artifacts/contracts/SwapRouter.sol/SwapRouter.json"),
  NFTDescriptor: require("@uniswap/v3-periphery/artifacts/contracts/libraries/NFTDescriptor.sol/NFTDescriptor.json"),
  NonfungibleTokenPositionDescriptor: require("@uniswap/v3-periphery/artifacts/contracts/NonfungibleTokenPositionDescriptor.sol/NonfungibleTokenPositionDescriptor.json"),
  NonfungiblePositionManager: require("@uniswap/v3-periphery/artifacts/contracts/NonfungiblePositionManager.sol/NonfungiblePositionManager.json"),
  WETH9: require("../Context/IWETH.json"), // We will use a simple WETH implementation or mock
};

// Helper to link libraries if needed (Uniswap V3 usually doesn't need external linking for core, but NFTDescriptor might)
const linkLibraries = ({ bytecode, linkReferences }, libraries) => {
  Object.keys(linkReferences).forEach((fileName) => {
    Object.keys(linkReferences[fileName]).forEach((contractName) => {
      if (!libraries.hasOwnProperty(contractName)) {
        throw new Error(`Missing link library name ${contractName}`);
      }
      const address = libraries[contractName].replace("0x", "").toLowerCase();
      const start = linkReferences[fileName][contractName][0].start * 2 + 2;
      const length = 20 * 2;
      bytecode = bytecode
        .slice(0, start)
        .concat(address)
        .concat(bytecode.slice(start + length));
    });
  });
  return bytecode;
};

async function main() {
  const [owner] = await ethers.getSigners();
  console.log("Deploying contracts with account:", owner.address);

  // 1. Deploy WETH (Mock)
  // Since we don't have the WETH source in node_modules easily accessible as an artifact, 
  // let's use a simple ERC20 that allows deposit/withdraw or just use the one we might have.
  // Actually, for simplicity, let's deploy the BooToken as WETH for now, or better, 
  // let's deploy a standard WETH contract if we can find one. 
  // The project has IWETH.json but maybe not the implementation.
  // Let's deploy a "WETH" token using the LifeToken contract as a base but renaming it? 
  // No, WETH needs deposit() function.
  // Let's assume the user has a WETH contract or we deploy a mock.
  // For this script, I'll deploy a "LifeToken" and call it WETH for simplicity of swapping, 
  // BUT real WETH is needed for the Router to unwrap ETH.
  // Let's try to find a WETH artifact or deploy a minimal one.
  
  // Minimal WETH Mock
  const WETHFactory = await ethers.getContractFactory("LifeToken"); // Re-using LifeToken as WETH placeholder if real WETH missing
  const weth = await WETHFactory.deploy();
  await weth.deployed();
  console.log("WETH (Mock) deployed to:", weth.address);

  // 2. Deploy Factory
  const Factory = new ethers.ContractFactory(
    artifacts.UniswapV3Factory.abi,
    artifacts.UniswapV3Factory.bytecode,
    owner
  );
  const factory = await Factory.deploy();
  await factory.deployed();
  console.log("Uniswap V3 Factory deployed to:", factory.address);

  // 3. Deploy Router
  const Router = new ethers.ContractFactory(
    artifacts.SwapRouter.abi,
    artifacts.SwapRouter.bytecode,
    owner
  );
  const router = await Router.deploy(factory.address, weth.address);
  await router.deployed();
  console.log("SwapRouter deployed to:", router.address);

  // 4. Deploy NFT Position Manager
  // Needs NFTDescriptor library
  const NFTDescriptor = new ethers.ContractFactory(
    artifacts.NFTDescriptor.abi,
    artifacts.NFTDescriptor.bytecode,
    owner
  );
  const nftDescriptor = await NFTDescriptor.deploy();
  await nftDescriptor.deployed();
  
  const linkedBytecode = linkLibraries(
    {
      bytecode: artifacts.NonfungibleTokenPositionDescriptor.bytecode,
      linkReferences: artifacts.NonfungibleTokenPositionDescriptor.linkReferences,
    },
    {
      NFTDescriptor: nftDescriptor.address,
    }
  );

  const NFTPositionDescriptor = new ethers.ContractFactory(
    artifacts.NonfungibleTokenPositionDescriptor.abi,
    linkedBytecode,
    owner
  );
  // Native currency label 'ETH'
  const nativeCurrencyLabelBytes = ethers.utils.formatBytes32String("ETH");
  const nftPositionDescriptor = await NFTPositionDescriptor.deploy(weth.address, nativeCurrencyLabelBytes);
  await nftPositionDescriptor.deployed();

  const PositionManager = new ethers.ContractFactory(
    artifacts.NonfungiblePositionManager.abi,
    artifacts.NonfungiblePositionManager.bytecode,
    owner
  );
  const positionManager = await PositionManager.deploy(factory.address, weth.address, nftPositionDescriptor.address);
  await positionManager.deployed();
  console.log("NonfungiblePositionManager deployed to:", positionManager.address);

  // 5. Deploy Tokens (BooToken, LifeToken)
  const BooToken = await ethers.getContractFactory("BooToken");
  const booToken = await BooToken.deploy();
  await booToken.deployed();
  console.log("BooToken deployed to:", booToken.address);

  const LifeToken = await ethers.getContractFactory("LifeToken");
  const lifeToken = await LifeToken.deploy();
  await lifeToken.deployed();
  console.log("LifeToken deployed to:", lifeToken.address);

  // 6. Deploy SingleSwapToken (User's contract)
  // We need to update the hardcoded addresses in SingleSwapToken or pass them in constructor.
  // The current SingleSwapToken likely has hardcoded addresses. We should ideally redeploy it with new addresses.
  // But SingleSwapToken.sol might not have a constructor for these.
  // Let's check SingleSwapToken.sol content.
  // It uses ISwapRouter(swapRouter).
  // We need to update SingleSwapToken to use our new Router address.
  // Since we can't easily change the solidity code dynamically, we might need to update the source file first?
  // Or maybe SingleSwapToken accepts router in constructor?
  // Let's assume we will update the source code later or it uses a constant we can't change easily without editing file.
  
  // For now, let's just print the addresses. The user will need to update constants.js AND likely SwapToken.sol 
  // if it hardcodes the router address.

  // 7. Initialize Pool (BooToken / WETH)
  // Fee 3000 (0.3%)
  const fee = 3000;
  // Encode price sqrt(1/1) * 2^96 = 79228162514264337593543950336
  const price = BigInt("79228162514264337593543950336"); 

  // Sort tokens to determine token0/token1
  const [token0, token1] = booToken.address.toLowerCase() < weth.address.toLowerCase() 
    ? [booToken.address, weth.address] 
    : [weth.address, booToken.address];
  
  console.log(`Initializing pool for ${token0} and ${token1}`);

  await positionManager.createAndInitializePoolIfNecessary(
    token0,
    token1,
    fee,
    price,
    { gasLimit: 5000000 }
  );
  console.log("Pool initialized for BooToken/WETH");

  // 8. Add Liquidity
  // Approve PositionManager
  const amountToApprove = ethers.utils.parseEther("1000000");
  await booToken.approve(positionManager.address, amountToApprove);
  await weth.approve(positionManager.address, amountToApprove);
  
  // Mint Liquidity
  // Sort tokens to determine token0/token1
  // Already sorted above
  
  const params = {
    token0: token0,
    token1: token1,
    fee: fee,
    tickLower: -887220, // Min tick
    tickUpper: 887220,  // Max tick
    amount0Desired: ethers.utils.parseEther("1000"),
    amount1Desired: ethers.utils.parseEther("1000"),
    amount0Min: 0,
    amount1Min: 0,
    recipient: owner.address,
    deadline: Math.floor(Date.now() / 1000) + 60 * 10,
  };

  const tx = await positionManager.mint(params, { gasLimit: 10000000 });
  await tx.wait();
  console.log("Liquidity added to pool");

  console.log("\nPLEASE UPDATE constants.js WITH THESE ADDRESSES:");
  console.log("FACTORY_ADDRESS:", factory.address);
  console.log("SWAP_ROUTER_ADDRESS:", router.address);
  console.log("NFT_MANAGER_ADDRESS:", positionManager.address);
  console.log("WETH_ADDRESS:", weth.address); // Add this to constants.js if not present
  console.log("BooTokenAddress:", booToken.address);
  console.log("LifeTokenAddress:", lifeToken.address);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
