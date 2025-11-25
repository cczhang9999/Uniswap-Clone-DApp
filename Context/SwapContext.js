import React, { useState, useEffect } from "react";
import { ethers } from "ethers";
import Web3Modal from "web3modal";

//INTERNAL IMPORT
import {
  checkIfWalletConnected,
  connectWallet,
  connectingWithBooToken,
  connectingWithLifeToken,
  connectingWithSingleSwapToken,
  connectingWithIcoToken,
  connectingWithMultiSwapToken,
} from "../Utils/appFeatures";
import IWETH from "./IWETH.json";
import { BooTokenAddress, LifeTokenAddress, WETH_ADDRESS } from "./constants";

export const SwapContext = React.createContext();

export const SwapProvider = ({ children }) => {
  const [account, setAccount] = useState("");
  const [ether, setEther] = useState("");
  const [networkConnect, setNetworkConnect] = useState("");
  const [weth9, setWeth9] = useState("");
  const [dai, setDai] = useState("");
  const [tokenData, setTokenData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const [topTokenList, setTopTokenList] = useState([
    {
      name: "BooToken",
      symbol: "BOO",
      tokenBalance: "0",
      tokenAddress: BooTokenAddress,
    },
    {
      name: "LifeToken",
      symbol: "LIFE",
      tokenBalance: "0",
      tokenAddress: LifeTokenAddress,
    },
    {
      name: "Wrapped Ether",
      symbol: "WETH",
      tokenBalance: "0",
      tokenAddress: WETH_ADDRESS,
    },
  ]);

  //FETCH DATA
  const fetchingData = async () => {
    try {
      //GET USER ACCOUNT
      const userAccount = await checkIfWalletConnected();
      setAccount(userAccount);
      //CREATE PROVIDER
      const web3modal = new Web3Modal();
      const connection = await web3modal.connect();
      const provider = new ethers.providers.Web3Provider(connection);
      const signer = provider.getSigner();
      //CHECK Balance
      const balance = await provider.getBalance(userAccount);
      const convertBal = ethers.utils.formatEther(balance);
      setEther(convertBal);

      //GET NETWORK
      const network = await provider.getNetwork();
      setNetworkConnect(network.name === "unknown" ? "Localhost" : network.name);
      
      // Fetch token balances
      const updatedTokenList = await Promise.all(
        topTokenList.map(async (token) => {
          try {
            const tokenContract = new ethers.Contract(
              token.tokenAddress,
              IWETH.abi,
              signer
            );
            const balance = await tokenContract.balanceOf(userAccount);
            const formattedBalance = ethers.utils.formatEther(balance);
            return {
              ...token,
              tokenBalance: parseFloat(formattedBalance).toFixed(2),
            };
          } catch (error) {
            console.log(`Error fetching balance for ${token.name}:`, error);
            return token;
          }
        })
      );
      setTopTokenList(updatedTokenList);
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    fetchingData();
  }, []);

  const connectWallet = async () => {
    try {
      if (!window.ethereum) return console.log("Install MetaMask");
      const accounts = await window.ethereum.request({
        method: "eth_requestAccounts",
      });
      setAccount(accounts[0]);

      // Check and switch network
      const chainId = await window.ethereum.request({ method: "eth_chainId" });
      const hardhatChainId = "0x7a69"; // 31337 in hex

      if (chainId !== hardhatChainId) {
        try {
          await window.ethereum.request({
            method: "wallet_switchEthereumChain",
            params: [{ chainId: hardhatChainId }],
          });
        } catch (switchError) {
          // This error code indicates that the chain has not been added to MetaMask.
          if (switchError.code === 4902) {
            try {
              await window.ethereum.request({
                method: "wallet_addEthereumChain",
                params: [
                  {
                    chainId: hardhatChainId,
                    chainName: "Localhost 8545",
                    rpcUrls: ["http://127.0.0.1:8545"],
                    nativeCurrency: {
                      name: "ETH",
                      symbol: "ETH",
                      decimals: 18,
                    },
                  },
                ],
              });
            } catch (addError) {
              console.log("Error adding chain:", addError);
            }
          } else {
            console.log("Error switching chain:", switchError);
          }
        }
      }

      window.location.reload();
    } catch (error) {
      console.log(error);
    }
  };

  //SINGLE SWAP
  const singleSwapToken = async ({ token1, token2, swapAmount }) => {
    try {
      setIsLoading(true);
      console.log(token1, token2, swapAmount);
      if (!token1?.tokenAddress || !token2?.tokenAddress) {
        console.log("Invalid token selection");
        setError("Please select both tokens");
        setIsLoading(false);
        return;
      }

      const web3modal = new Web3Modal();
      const connection = await web3modal.connect();
      const provider = new ethers.providers.Web3Provider(connection);
      const signer = provider.getSigner();
      const userAddress = await signer.getAddress();

      // APPROVE TOKEN
      // Dynamically create contract for token1 to approve
      const contract = new ethers.Contract(token1.tokenAddress, IWETH.abi, signer);
      const swapTokenContract = await connectingWithSingleSwapToken();

      const amountIn = ethers.utils.parseUnits(swapAmount, 18);

      await contract.approve(swapTokenContract.address, amountIn);
      
      const transaction = await swapTokenContract.swapExactInputSingle(
        token1.tokenAddress,
        token2.tokenAddress,
        amountIn
      );
      await transaction.wait();
      console.log("Swap completed:", transaction);
      
      await fetchingData();
      setIsLoading(false);
    } catch (error) {
      console.log(error);
      setError("Swap failed");
      setIsLoading(false);
    }
  };

  return (
    <SwapContext.Provider
      value={{
        account,
        weth9,
        dai,
        networkConnect,
        ether,
        connectWallet,
        tokenData,
        topTokenList,
        isLoading,
        error,
        singleSwapToken,
      }}
    >
      {children}
    </SwapContext.Provider>
  );
};
