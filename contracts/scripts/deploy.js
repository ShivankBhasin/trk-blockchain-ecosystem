const hre = require("hardhat");

async function main() {
  const [deployer] = await hre.ethers.getSigners();

  console.log("Deploying contracts with account:", deployer.address);
  console.log("Account balance:", (await deployer.provider.getBalance(deployer.address)).toString());

  // USDT address on BSC
  // Mainnet: 0x55d398326f99059fF775485246999027B3197955
  // Testnet: Use a mock or deploy your own
  const USDT_ADDRESS = process.env.USDT_ADDRESS || "0x55d398326f99059fF775485246999027B3197955";

  console.log("\n1. Deploying TRKBlockchain...");
  const TRKBlockchain = await hre.ethers.getContractFactory("TRKBlockchain");
  const trkBlockchain = await TRKBlockchain.deploy(USDT_ADDRESS);
  await trkBlockchain.waitForDeployment();
  const trkBlockchainAddress = await trkBlockchain.getAddress();
  console.log("TRKBlockchain deployed to:", trkBlockchainAddress);

  console.log("\n2. Deploying TRKLuckyDraw...");
  const TRKLuckyDraw = await hre.ethers.getContractFactory("TRKLuckyDraw");
  const trkLuckyDraw = await TRKLuckyDraw.deploy(USDT_ADDRESS);
  await trkLuckyDraw.waitForDeployment();
  const trkLuckyDrawAddress = await trkLuckyDraw.getAddress();
  console.log("TRKLuckyDraw deployed to:", trkLuckyDrawAddress);

  console.log("\n3. Deploying TRKClubIncome...");
  const TRKClubIncome = await hre.ethers.getContractFactory("TRKClubIncome");
  const trkClubIncome = await TRKClubIncome.deploy(USDT_ADDRESS);
  await trkClubIncome.waitForDeployment();
  const trkClubIncomeAddress = await trkClubIncome.getAddress();
  console.log("TRKClubIncome deployed to:", trkClubIncomeAddress);

  console.log("\n4. Setting up contract connections...");

  // Set main contract for LuckyDraw
  await trkLuckyDraw.setMainContract(trkBlockchainAddress);
  console.log("LuckyDraw main contract set");

  // Set main contract for ClubIncome
  await trkClubIncome.setMainContract(trkBlockchainAddress);
  console.log("ClubIncome main contract set");

  console.log("\n========================================");
  console.log("DEPLOYMENT COMPLETE!");
  console.log("========================================");
  console.log("\nContract Addresses:");
  console.log("- TRKBlockchain:", trkBlockchainAddress);
  console.log("- TRKLuckyDraw:", trkLuckyDrawAddress);
  console.log("- TRKClubIncome:", trkClubIncomeAddress);
  console.log("\nUSDT Address:", USDT_ADDRESS);

  // Save addresses to file for frontend
  const fs = require("fs");
  const addresses = {
    TRKBlockchain: trkBlockchainAddress,
    TRKLuckyDraw: trkLuckyDrawAddress,
    TRKClubIncome: trkClubIncomeAddress,
    USDT: USDT_ADDRESS,
    network: hre.network.name,
    chainId: hre.network.config.chainId,
    deployedAt: new Date().toISOString(),
  };

  fs.writeFileSync(
    "./deployed-addresses.json",
    JSON.stringify(addresses, null, 2)
  );
  console.log("\nAddresses saved to deployed-addresses.json");

  // Verify on BSCScan if not local
  if (hre.network.name !== "hardhat" && hre.network.name !== "localhost") {
    console.log("\nWaiting for block confirmations before verification...");
    await new Promise((r) => setTimeout(r, 30000)); // Wait 30 seconds

    console.log("\nVerifying contracts on BSCScan...");
    try {
      await hre.run("verify:verify", {
        address: trkBlockchainAddress,
        constructorArguments: [USDT_ADDRESS],
      });
      console.log("TRKBlockchain verified!");
    } catch (e) {
      console.log("TRKBlockchain verification failed:", e.message);
    }

    try {
      await hre.run("verify:verify", {
        address: trkLuckyDrawAddress,
        constructorArguments: [USDT_ADDRESS],
      });
      console.log("TRKLuckyDraw verified!");
    } catch (e) {
      console.log("TRKLuckyDraw verification failed:", e.message);
    }

    try {
      await hre.run("verify:verify", {
        address: trkClubIncomeAddress,
        constructorArguments: [USDT_ADDRESS],
      });
      console.log("TRKClubIncome verified!");
    } catch (e) {
      console.log("TRKClubIncome verification failed:", e.message);
    }
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
