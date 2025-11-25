// SPDX-License-Identifier: MIT
pragma solidity >=0.7.6;
pragma abicoder v2;

// 引入 Uniswap V3 的 SwapRouter 接口，用于执行交易
import "@uniswap/v3-periphery/contracts/interfaces/ISwapRouter.sol";
// 引入 TransferHelper 库，用于安全的代币转账和授权
import "@uniswap/v3-periphery/contracts/libraries/TransferHelper.sol";

contract SwapMultiHop {
    // 定义不可变的 SwapRouter 接口实例
    ISwapRouter public immutable swapRouter;
    // 定义不可变的 WETH9 地址
    address public immutable WETH9;

    // 构造函数：在部署合约时初始化 swapRouter 和 WETH9 地址
    constructor(ISwapRouter _swapRouter, address _weth9) {
        swapRouter = _swapRouter;
        WETH9 = _weth9;
    }

    // 定义常量代币地址 (注意：这些是主网地址，本地测试时仅作示例或需替换为 Mock 地址)
    address public constant DAI = 0x6B175474E89094C44Da98b954EedeAC495271d0F;
    address public constant USDC = 0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48;

    // 多跳精确输入交换函数
    // amountIn: 用户想要卖出的 WETH 数量
    // amountOut: 函数返回用户最终买到的 DAI 数量
    function swapExactInputMultihop(uint256 amountIn)
        external
        returns (uint256 amountOut)
    {
        // 1. 将用户的 WETH 转入本合约
        TransferHelper.safeTransferFrom(
            WETH9,
            msg.sender,    // 发送者（用户）
            address(this), // 接收者（本合约）
            amountIn
        );

        // 2. 授权 SwapRouter 使用本合约的 WETH
        TransferHelper.safeApprove(WETH9, address(swapRouter), amountIn);

        // 3. 构建多跳交易参数
        // 路径: WETH -> USDC (费率 0.3%) -> DAI (费率 0.01%)
        ISwapRouter.ExactInputParams memory params = ISwapRouter
            .ExactInputParams({
                path: abi.encodePacked(
                    WETH9,
                    uint24(3000), // WETH/USDC 池费率 0.3%
                    USDC,
                    uint24(100),  // USDC/DAI 池费率 0.01%
                    DAI
                ),
                recipient: msg.sender, // 最终代币接收者（用户）
                deadline: block.timestamp,
                amountIn: amountIn,
                amountOutMinimum: 0 // 最少买到多少（0表示不限制，生产环境需设置）
            });

        // 4. 执行交易
        amountOut = swapRouter.exactInput(params);
    }

    // 多跳精确输出交换函数
    // amountOut: 用户想要买到的 DAI 数量
    // amountInMaximum: 用户愿意支付的最大 WETH 数量
    // amountIn: 函数返回实际花费的 WETH 数量
    function swapExactOutputMultihop(uint256 amountOut, uint256 amountInMaximum)
        external
        returns (uint256 amountIn)
    {
        // 1. 先将用户愿意支付的最大 WETH 数量转入本合约
        TransferHelper.safeTransferFrom(
            WETH9,
            msg.sender,
            address(this),
            amountInMaximum
        );

        // 2. 授权 SwapRouter 使用这些 WETH
        TransferHelper.safeApprove(WETH9, address(swapRouter), amountInMaximum);

        // 3. 构建多跳交易参数
        // 注意：精确输出时，路径是反向编码的
        // 路径: DAI <- USDC (费率 0.01%) <- WETH (费率 0.3%)
        ISwapRouter.ExactOutputParams memory params = ISwapRouter
            .ExactOutputParams({
                path: abi.encodePacked(
                    DAI,
                    uint24(100),  // USDC/DAI 池费率 0.01%
                    USDC,
                    uint24(3000), // WETH/USDC 池费率 0.3%
                    WETH9
                ),
                recipient: msg.sender,
                deadline: block.timestamp,
                amountOut: amountOut,
                amountInMaximum: amountInMaximum
            });

        // 4. 执行交易
        amountIn = swapRouter.exactOutput(params);

        // 5. 如果实际花费少于最大支付额，退还多余的 WETH 给用户
        if (amountIn < amountInMaximum) {
            // 重置授权为 0 (这是一个好习惯，虽然不是必须的)
            TransferHelper.safeApprove(WETH9, address(swapRouter), 0);
            // 退款
            TransferHelper.safeTransfer(
                WETH9,
                msg.sender,
                amountInMaximum - amountIn
            );
        }
    }
}
