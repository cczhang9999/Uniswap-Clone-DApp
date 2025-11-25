import { ethers } from "ethers";
import web3Modal from "web3modal";

//INTERNAL IMPORT
import factoryAbi from "./factoryAbi.json";
import ERC20ABI from "./ERC20ABI.json";
import SwapRouterABI from "./SwapRouterABI.json";
import NFTManagerABI from "./NFTManagerABI.json";

import SingleSwapToken from "../artifacts/contracts/SwapToken.sol/SingleSwapToken.json";
import SwapMultiHop from "../artifacts/contracts/SwapMultiHop.sol/SwapMultiHop.json";

export const BooTokenABI = ERC20ABI;
export const LifeTokenABI = ERC20ABI;
export const SingleSwapTokenABI = SingleSwapToken.abi;
export const SwapMultiHopABI = SwapMultiHop.abi;

//ADDRESS
export const FACTORY_ADDRESS = "0x9E545E3C0baAB3E08CdfD552C960A1050f373042";
export const SWAP_ROUTER_ADDRESS = "0xa82fF9aFd8f496c3d6ac40E2a0F282E47488CFc9";
export const NFT_MANAGER_ADDRESS = "0xf5059a5D33d5853360D16C683c16e67980206f36";

export const BooTokenAddress = "0x95401dc811bb5740090279Ba06cfA8fcF6113778";
export const LifeTokenAddress = "0x998abeb3E57409262aE5b751f60747921B33613E";
export const SingleSwapTokenAddress = "0x809d550fca64d94Bd9F66E60752A544199cfAC3D"; 
export const SwapMultiHopAddress = "0x4c5859f0F772848b2D91F1D83E2Fe57935348029";
export const WETH_ADDRESS = "0x84eA74d481Ee0A5332c457a4d796187F6Ba67fEB";

// Helper to shorten address
export const shortenAddress = (address) =>
  `${address?.slice(0, 5)}...${address?.slice(address.length - 4)}`;

// Helper to parse error messages
export const parseErrorMsg = (e) => {
  const json = JSON.parse(JSON.stringify(e));
  return json?.reason || json?.error?.message || e?.message || "Error";
};
