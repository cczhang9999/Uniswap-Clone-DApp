// SPDX-License-Identifier: MIT
pragma solidity >=0.7.6;
pragma abicoder v2;

import "@uniswap/v3-periphery/contracts/interfaces/ISwapRouter.sol";
import "@uniswap/v3-periphery/contracts/libraries/TransferHelper.sol";

contract SingleSwapToken {
    ISwapRouter public immutable swapRouter;
    address public immutable WETH9;

    constructor(ISwapRouter _swapRouter, address _weth9) {
        swapRouter = _swapRouter;
        WETH9 = _weth9;
    }

    // 定义 DAI 和 USDC 的地址常量
    address public constant DAI = 0x6B175474E89094C44Da98b954EedeAC495271d0F;
    address public constant USDC = 0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48;

    /// @notice swapExactInputSingle 使用精确的输入金额交换代币
    /// @dev msg.sender 必须先批准此合约花费至少 amountIn 的 tokenIn
    /// @param amountIn 输入代币的金额
    /// @return amountOut 收到的输出代币金额
    function swapExactInputSingle(
        address tokenIn,
        address tokenOut,
        uint256 amountIn
    ) external returns (uint256 amountOut) {
        // 将指定金额的 tokenIn 从 msg.sender 转移到本合约
        TransferHelper.safeTransferFrom(
            tokenIn,
            msg.sender,
            address(this),
            amountIn
        );

        // 批准 swapRouter 花费 tokenIn
        TransferHelper.safeApprove(tokenIn, address(swapRouter), amountIn);

        // 设置单跳交换的参数
        ISwapRouter.ExactInputSingleParams memory params = ISwapRouter
            .ExactInputSingleParams({
                tokenIn: tokenIn,
                tokenOut: tokenOut,
                fee: 3000, // 费率 0.3%
                recipient: msg.sender, // 接收者为调用者
                deadline: block.timestamp, // 截止时间为当前块时间
                amountIn: amountIn,
                amountOutMinimum: 0, // 在生产环境中，应设置为非零值以避免滑点过大
                sqrtPriceLimitX96: 0 // 0 表示不设置价格限制
            });

        // 执行交换
        amountOut = swapRouter.exactInputSingle(params);
    }

    /// @notice swapExactOutputSingle 使用精确的输出金额交换代币
    /// @dev msg.sender 必须先批准此合约花费至少 amountInMaximum 的输入代币
    /// @param amountOut 期望收到的输出代币金额
    /// @param amountInMaximum 愿意支付的最大输入代币金额
    /// @return amountIn 实际支付的输入代币金额
    function swapExactOutputSingle(uint256 amountOut, uint256 amountInMaximum)
        external
        returns (uint256 amountIn)
    {
        // 将最大金额的 WETH9 从 msg.sender 转移到本合约
        TransferHelper.safeTransferFrom(
            WETH9,
            msg.sender,
            address(this),
            amountInMaximum
        );

        // 批准 swapRouter 花费 WETH9
        TransferHelper.safeApprove(WETH9, address(swapRouter), amountInMaximum);

        // 设置单跳交换的参数
        ISwapRouter.ExactOutputSingleParams memory params = ISwapRouter
            .ExactOutputSingleParams({
                tokenIn: WETH9,
                tokenOut: DAI,
                fee: 3000, // 费率 0.3%
                recipient: msg.sender, // 接收者为调用者
                deadline: block.timestamp, // 截止时间
                amountOut: amountOut,
                amountInMaximum: amountInMaximum,
                sqrtPriceLimitX96: 0 // 0 表示不设置价格限制
            });

        // 执行交换，返回实际花费的输入代币数量
        amountIn = swapRouter.exactOutputSingle(params);

        // 如果实际花费少于最大金额，将剩余的 WETH9 退还给 msg.sender
        if (amountIn < amountInMaximum) {
            // 重置批准金额为 0 (这是一个好习惯，虽然在这里不是严格必须的，因为我们要退款)
            TransferHelper.safeApprove(WETH9, address(swapRouter), 0);
            // 退还剩余的代币
            TransferHelper.safeTransfer(
                WETH9,
                msg.sender,
                amountInMaximum - amountIn
            );
        }
    }
}
