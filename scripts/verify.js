const hre = require("hardhat");

async function main() {
  const [owner] = await hre.ethers.getSigners();
  console.log("Verifying contracts with account:", owner.address);

  const BooTokenAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const LifeTokenAddress = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";

  const BooToken = await hre.ethers.getContractAt("BooToken", BooTokenAddress);
  const LifeToken = await hre.ethers.getContractAt("LifeToken", LifeTokenAddress);

  const balanceBoo = await BooToken.balanceOf(owner.address);
  const balanceLife = await LifeToken.balanceOf(owner.address);

  console.log("BooToken Balance:", hre.ethers.utils.formatEther(balanceBoo));
  console.log("LifeToken Balance:", hre.ethers.utils.formatEther(balanceLife));

  if (balanceBoo.gt(0) && balanceLife.gt(0)) {
    console.log("Verification Successful: Tokens minted and balances correct.");
  } else {
    console.log("Verification Failed: Zero balance.");
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
