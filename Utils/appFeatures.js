import { ethers } from "ethers";
import Web3Modal from "web3modal";

import {
  BooTokenAddress,
  BooTokenABI,
  LifeTokenAddress,
  LifeTokenABI,
  SingleSwapTokenAddress,
  SingleSwapTokenABI,
  SwapMultiHopAddress,
  SwapMultiHopABI,
  IWETHAddress,
  IWETHABI,
} from "../Context/constants";

//CHECK IF WALLET CONNECT
export const checkIfWalletConnected = async () => {
  try {
    if (!window.ethereum) return console.log("Install MetaMask");
    const accounts = await window.ethereum.request({
      method: "eth_accounts",
    });
    const firstAccount = accounts[0];
    return firstAccount;
  } catch (error) {
    console.log(error);
  }
};

//CONNECT WALLET
export const connectWallet = async () => {
  try {
    if (!window.ethereum) return console.log("Install MetaMask");
    const accounts = await window.ethereum.request({
      method: "eth_requestAccounts",
    });
    const firstAccount = accounts[0];
    return firstAccount;
  } catch (error) {
    console.log(error);
  }
};

//FETCHING CONTRACT
const fetchContract = (address, abi, signer) =>
  new ethers.Contract(address, abi, signer);

export const connectingWithBooToken = async () => {
  try {
    const web3modal = new Web3Modal();
    const connection = await web3modal.connect();
    const provider = new ethers.providers.Web3Provider(connection);
    const signer = provider.getSigner();
    const contract = fetchContract(BooTokenAddress, BooTokenABI, signer);
    return contract;
  } catch (error) {
    console.log(error);
  }
};

export const connectingWithLifeToken = async () => {
  try {
    const web3modal = new Web3Modal();
    const connection = await web3modal.connect();
    const provider = new ethers.providers.Web3Provider(connection);
    const signer = provider.getSigner();
    const contract = fetchContract(LifeTokenAddress, LifeTokenABI, signer);
    return contract;
  } catch (error) {
    console.log(error);
  }
};

export const connectingWithSingleSwapToken = async () => {
  try {
    const web3modal = new Web3Modal();
    const connection = await web3modal.connect();
    const provider = new ethers.providers.Web3Provider(connection);
    const signer = provider.getSigner();
    const contract = fetchContract(
      SingleSwapTokenAddress,
      SingleSwapTokenABI,
      signer
    );
    return contract;
  } catch (error) {
    console.log(error);
  }
};

export const connectingWithIcoToken = async () => {
  try {
    const web3modal = new Web3Modal();
    const connection = await web3modal.connect();
    const provider = new ethers.providers.Web3Provider(connection);
    const signer = provider.getSigner();
    const contract = fetchContract(BooTokenAddress, BooTokenABI, signer);
    return contract;
  } catch (error) {
    console.log(error);
  }
};

export const connectingWithMultiSwapToken = async () => {
  try {
    const web3modal = new Web3Modal();
    const connection = await web3modal.connect();
    const provider = new ethers.providers.Web3Provider(connection);
    const signer = provider.getSigner();
    const contract = fetchContract(
      SwapMultiHopAddress,
      SwapMultiHopABI,
      signer
    );
    return contract;
  } catch (error) {
    console.log(error);
  }
};
