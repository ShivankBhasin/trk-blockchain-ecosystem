export const environment = {
  production: false,
  // Backend API (optional - for off-chain data indexing)
  apiUrl: 'http://localhost:8080/api',
  // BSC Testnet contract addresses (update after deployment)
  contracts: {
    trkBlockchain: '0xd8b934580fcE35a11B58C6D73aDeE468a2833fa8',
    luckyDraw: '0xD7ACd2a9FD159E69Bb102A1ca21C9a3e3A5F771B',
    clubIncome: '0xf8e81D47203A594245E36C48e151709F0C19fBe8',
    usdt: '0x337610d27c682E347C9cD60BD4b3b107C9d34dDd' // BSC Testnet USDT
  },
  // BSC Testnet
  chainId: 97,
  chainName: 'BSC Testnet',
  rpcUrl: 'https://data-seed-prebsc-1-s1.binance.org:8545',
  explorerUrl: 'https://testnet.bscscan.com'
};
