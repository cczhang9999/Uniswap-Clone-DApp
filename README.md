
# Building and Deploying a Uniswap Exchange Clone for DeFi

Enter the world of decentralized finance (DeFi) with our Uniswap Exchange clone, UniClone. This project focuses on replicating the functionalities of the popular decentralized exchange Uniswap, providing users with a seamless and efficient platform for swapping tokens, providing liquidity, and earning fees.

UniClone leverages smart contracts and blockchain technology to enable trustless and permissionless token swaps directly from users' wallets. By replicating Uniswap's automated market maker (AMM) mechanism, our clone ensures liquidity for all listed tokens and allows users to trade without the need for traditional order books or centralized intermediaries.

## Project Overview

![alt text](https://www.daulathussain.com/wp-content/uploads/2023/04/Uniswap-clone.jpg)

## Instruction

Kindly follow the following Instructions to run the project in your system and install the necessary requirements


- [Final Source Code](https://www.theblockchaincoders.com/sourceCode/build-uniswap-dapp-project-source-code)

#### Setup Video
- [Final Code Setup video](https://youtu.be/NAuuGa_7oro?si=hHXzjfPR_mPQx78p)

```https://code.visualstudio.com/download
  WATCH: Setup & Demo Of Project
```

#### Install Vs Code Editor

```https://code.visualstudio.com/download
  GET: VsCode Editor
```

#### NodeJs & NPM Version

```https://nodejs.org/en/download
  NodeJs: v18.12.1
  NPM: 8.19.2
```

#### Clone Starter File

```https://github.com/daulathussain/Airdrop-Crypto-Starter-File
  GET: Project Starter File Download
```


All you need to follow the complete project and follow the instructions which are explained in the tutorial by Daulat

## Final Code Instruction

If you download the final source code then you can follow the following instructions to run the Dapp successfully

#### Setup Video

```https://code.visualstudio.com/download
  WATCH: Setup & Demo Of Project
```

#### Final Source Code

```https://www.theblockchaincoders.com/SourceCode
  Download the Final Source Code
```

#### Install Vs Code Editor

```https://code.visualstudio.com/download
  GET: VsCode Editor
```

#### NodeJs & NPM Version

```https://nodejs.org/en/download
  NodeJs: v18.12.1
  NPM: 8.19.2
```


#### Test Faucets

Alchemy will provide you with some free test faucets which you can transfer to your wallet address for deploying the contract

```https://www.alchemy.com/faucets
  Get: Free Test Faucets
```

#### RemixID

We are using RemixID for deploying the contract and generation of the ABI in the project, but you can use any other tools like Hardhat, etc.

```https://remix-project.org
  OPEN: RemixID
```

#### Polygon Mumbai

```https://mumbai.polygonscan.com/
  OPEN: Polygon Mumbai
```

## Important Links

- [Get Pro Blockchain Developer Course](https://www.theblockchaincoders.com/pro-nft-marketplace)
- [Support Creator](https://bit.ly/Support-Creator)
- [All Projects Source Code](https://www.theblockchaincoders.com/SourceCode)


## Authors

- [@theblockchaincoders.com](https://www.theblockchaincoders.com/)
- [@consultancy](https://www.theblockchaincoders.com/consultancy)
- [@youtube](https://www.youtube.com/@daulathussain)


# IMPORTANT INFO

# Address

MAINNEXT TOKEN ADDRESS

"0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
"0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
"0xdAC17F958D2ee523a2206206994597C13D831ec7",
"0xB8c77482e45F1F44dE1745F52C74426C631bDD52",
"0x7D1AfA7B718fb893dB30A3aBc0Cfc608AaCfeBB0",
"0x6B175474E89094C44Da98b954EedeAC495271d0F",
"0x95aD61b0a150d79219dCF64E1E6Cc01f0B64C4cE",
"0x4278C5d322aB92F1D876Dd7Bd9b44d1748b88af2",
"0x0D92d35D311E54aB8EEA0394d7E773Fc5144491a",
"0x24EcC5E6EaA700368B8FAC259d3fBD045f695A08",

ISwapRouter(0xE592427A0AEce92De3Edee1F18E0157C05861564);

TEST

const DAI = "0x6B175474E89094C44Da98b954EedeAC495271d0F";
const USDC = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48";
const DAI_WHALE = "0x97f991971a37D4Ca58064e6a98FC563F03A71E5c";
const USDC_WHALE = "0x97f991971a37D4Ca58064e6a98FC563F03A71E5c";

const WETH9 = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2";

const qutorAddress = "0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6";

ETHERSCAN URL: `https://api.etherscan.io/api?module=contract&action=getabi&address=${address}&apikey=${ETHERSCAN_API_KEY}`;

V3_SWAP_ROUTER_ADDRESS = "0x68b3465833fb72A70ecDF485E0e4C7bD8665Fc45";

const name0 = "Wrapped Ether";
const symbol0 = "WETH";
const decimals0 = 18;
const address0 = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2";

const name1 = "DAI";
const symbol1 = "DAI";
const decimals1 = 18;
const address1 = "0x6B175474E89094C44Da98b954EedeAC495271d0F";

# //SECOND PACKAGE.JSON FILE

{
"name": "uniswapclone",
"version": "0.1.0",
"private": true,
"scripts": {
"dev": "next dev",
"build": "next build",
"start": "next start",
"lint": "next lint"
},
"dependencies": {
"axios": "^1.2.1",
"ethers": "^5.7.2",
"next": "13.0.3",
"react": "18.2.0",
"react-dom": "18.2.0",
"web3modal": "^1.9.9",
"@nomicfoundation/hardhat-chai-matchers": "^1.0.6",
"@nomicfoundation/hardhat-network-helpers": "^1.0.8",
"@uniswap/smart-order-router": "^2.5.30",
"@nomicfoundation/hardhat-toolbox": "^2.0.2",
"@nomiclabs/hardhat-ethers": "^2.2.3",
"@nomiclabs/hardhat-etherscan": "^3.1.7",
"@openzeppelin/contracts": "^4.8.3",
"@typechain/ethers-v5": "^10.2.1",
"@typechain/hardhat": "^6.1.6",
"@types/chai": "^4.3.5",
"@types/mocha": "^10.0.1",
"@uniswap/v3-periphery": "^1.4.3",
"@uniswap/v3-sdk": "^3.9.0",
"bignumber.js": "^9.1.1",
"chai": "^4.3.7",
"dotenv": "^16.0.3",
"hardhat": "^2.14.0",
"hardhat-gas-reporter": "^1.0.9",
"solidity-coverage": "^0.8.2",
"ts-node": "^10.9.1",
"typechain": "^8.1.1",
"typescript": "^5.0.4"
}
}

Uniswap Clone DApp 项目核心文档
本文档详细说明了该 Uniswap Clone 项目的交互流程、核心代码逻辑以及本地环境的运作方式。

1. 项目架构概述
本项目是一个基于 Uniswap V3 协议的去中心化交易应用 (DApp)。它主要由以下几部分组成：

前端 (Frontend): 使用 Next.js 和 React 构建，负责用户界面展示和交互。
状态管理 (State Management): 使用 React Context API (
SwapContext.js
) 管理全局状态（如钱包连接、代币数据、交易状态）。
智能合约 (Smart Contracts):
Uniswap V3 Core & Periphery: 标准的 Uniswap V3 合约（Factory, Router, PositionManager 等），用于底层交易和流动性管理。
User Contracts: 用户自定义的合约（如 SingleSwapToken.sol），作为与 Uniswap Router 交互的中间层。
Tokens: ERC20 代币合约（BooToken, LifeToken, Mock WETH）。
本地区块链 (Local Blockchain): 使用 Hardhat 运行本地以太坊节点，模拟真实的区块链环境。
2. 交互流程 (Interaction Flow)
当用户在前端点击 "Swap" 按钮时，系统内部发生了以下交互：

用户操作: 用户在 
HeroSection
 组件中输入兑换数量，点击 Swap。
前端调用: 组件调用 
SwapContext.js
 中的 
singleSwapToken
 函数。
授权 (Approve):
前端首先检查用户是否授权了合约使用其代币。
如果没有，前端会调用代币合约的 approve 方法，授权 
SingleSwapToken
 合约支配用户的代币。
执行交易 (Execute Swap):
前端调用 
SingleSwapToken
 合约的 swapExactInputSingle 方法。
该方法内部调用 Uniswap V3 SwapRouter 的 exactInputSingle 接口。
链上结算:
Uniswap Router 在流动性池中进行代币交换。
用户的输入代币被转入池子，输出代币从池子转给用户。
状态更新:
前端等待交易被打包确认 (transaction.wait())。
确认后，前端重新获取用户余额 (
fetchingData
) 并更新 UI。
3. 核心代码解析
3.1 全局状态管理 (
Context/SwapContext.js
)
这是前端的核心大脑，负责连接钱包和调用合约。

// 核心函数：执行单跳代币兑换
const singleSwapToken = async ({ token1, token2, swapAmount }) => {
  try {
    // ...省略部分代码...
    // 1. 连接钱包和获取签名者
    const web3modal = new Web3Modal();
    const connection = await web3modal.connect();
    const provider = new ethers.providers.Web3Provider(connection);
    const signer = provider.getSigner();
    // 2. 创建代币合约实例，用于授权
    // token1.tokenAddress 是用户要卖出的代币地址
    const contract = new ethers.Contract(token1.tokenAddress, IWETH.abi, signer);
    
    // 3. 获取我们部署的 Swap 合约实例
    const swapTokenContract = await connectingWithSingleSwapToken();
    // 4. 将用户输入的数量转换为 Wei (18位小数)
    const amountIn = ethers.utils.parseUnits(swapAmount, 18);
    // 5. 【关键步骤】授权 Swap 合约使用用户的代币
    // 必须先授权，否则合约无法把你的钱转走去交易
    await contract.approve(swapTokenContract.address, amountIn);
    
    // 6. 调用合约进行交易
    const transaction = await swapTokenContract.swapExactInputSingle(
      token1.tokenAddress, // 输入代币地址
      token2.tokenAddress, // 输出代币地址
      amountIn             // 输入数量
    );
    // 7. 等待交易在区块链上确认
    await transaction.wait();
    
    // 8. 刷新数据（余额等）
    await fetchingData();
    
  } catch (error) {
    console.log(error);
    // ...错误处理...
  }
};
3.2 智能合约 (
contracts/SwapToken.sol
)
这是部署在链上的合约，它封装了 Uniswap V3 的调用逻辑。

// SPDX-License-Identifier: MIT
pragma solidity >=0.7.6;
pragma abicoder v2;
import "@uniswap/v3-periphery/contracts/interfaces/ISwapRouter.sol";
import "@uniswap/v3-periphery/contracts/libraries/TransferHelper.sol";
contract SingleSwapToken {
    // Uniswap V3 的路由合约接口
    ISwapRouter public immutable swapRouter;
    // WETH 地址
    address public immutable WETH9;
    // 构造函数：部署时传入 Router 和 WETH 地址
    constructor(ISwapRouter _swapRouter, address _weth9) {
        swapRouter = _swapRouter;
        WETH9 = _weth9;
    }
    // 核心函数：精确输入单跳交换
    // amountIn: 用户想要卖出的具体数量
    // amountOut: 函数返回用户买到的数量
    function swapExactInputSingle(
        address tokenIn,
        address tokenOut,
        uint256 amountIn
    ) external returns (uint256 amountOut) {
        // 1. 将用户的代币转入本合约
        // 前端必须先调用 approve，这里才能 transferFrom 成功
        TransferHelper.safeTransferFrom(
            tokenIn,
            msg.sender,    // 谁调用的（用户）
            address(this), // 转给谁（本合约）
            amountIn
        );
        // 2. 本合约授权 Uniswap Router 使用这些代币
        TransferHelper.safeApprove(tokenIn, address(swapRouter), amountIn);
        // 3. 构建 Uniswap V3 交易参数
        ISwapRouter.ExactInputSingleParams memory params = ISwapRouter
            .ExactInputSingleParams({
                tokenIn: tokenIn,
                tokenOut: tokenOut,
                fee: 3000,              // 费率 0.3% (需与池子费率一致)
                recipient: msg.sender,  // 交易成功后，钱发给谁（发回给用户）
                deadline: block.timestamp,
                amountIn: amountIn,
                amountOutMinimum: 0,    // 最少买到多少（0表示不限制滑点，生产环境需设置）
                sqrtPriceLimitX96: 0    // 价格限制（0表示不限制）
            });
        // 4. 调用 Uniswap Router 执行真正的交易
        amountOut = swapRouter.exactInputSingle(params);
    }
}
4. 本地环境部署脚本 (
scripts/deploy_uniswap_local.js
)
为了在本地运行，我们需要自己搭建一套 Uniswap 环境。这个脚本做了以下事情：

部署 Mock WETH: 模拟以太坊上的 WETH。
部署 Factory: Uniswap V3 工厂合约，用于创建流动性池。
部署 Router: 路由合约，用于查找路径和执行交易。
部署 PositionManager: 用于管理流动性（添加/移除流动性）。
部署代币: BooToken 和 LifeToken。
初始化池子: 创建 BooToken / WETH 交易对，并设置初始价格。
添加流动性: 向池子中注入代币，这样用户才能进行交易。
5. 常见问题排查
Error: Transaction reverted: 通常是因为没有对应的流动性池，或者 WETH 地址不匹配。
余额不更新: 交易后没有调用 
fetchingData
 刷新数据，或者没有等待 transaction.wait()。
Invalid address: 前端传递给合约的代币地址为空或格式错误。


