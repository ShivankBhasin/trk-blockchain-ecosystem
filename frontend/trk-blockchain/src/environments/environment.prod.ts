export const environment = {
  production: true,
  // Backend API (optional - for off-chain data indexing)
  apiUrl: 'https://trk-blockchain-api.onrender.com/api',
  // BSC Mainnet contract addresses (update after deployment)
  contracts: {
    trkBlockchain: '0x0000000000000000000000000000000000000000',
    luckyDraw: '0x0000000000000000000000000000000000000000',
    clubIncome: '0x0000000000000000000000000000000000000000',
    usdt: '0x55d398326f99059fF775485246999027B3197955' // BSC Mainnet USDT
  },
  // BSC Mainnet
  chainId: 56,
  chainName: 'BNB Smart Chain',
  rpcUrl: 'https://bsc-dataseed.binance.org/',
  explorerUrl: 'https://bscscan.com'
};
