// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "./IERC20.sol";

/**
 * @title TRK Blockchain Real Cash Game Ecosystem
 * @notice Decentralized gaming platform on BSC with 7 income streams
 * @dev Built for Binance Smart Chain (BEP-20) with USDT
 */
contract TRKBlockchain {
    // ============ State Variables ============

    IERC20 public usdt;
    address public owner;

    // User structure - identified by wallet address only
    struct User {
        bool isRegistered;
        address referrer;
        uint256 registrationTime;
        uint256 totalDeposited;
        uint256 totalWithdrawn;
        uint256 directReferrals;
        uint256 teamSize;
        bool isActivated; // 10+ USDT deposited
        bool isPremiumActivated; // 100+ USDT deposited
        uint256 totalLosses;
        uint256 cashbackReceived;
        uint256 lastCashbackTime;
    }

    // Wallet structure - 4 types per user
    struct Wallets {
        uint256 cashBalance;      // For playing cash games
        uint256 practiceBalance;  // Non-withdrawable practice credits
        uint256 directWallet;     // Withdrawable earnings
        uint256 luckyDrawWallet;  // For lucky draw participation
    }

    // Game result
    struct GameResult {
        uint256 timestamp;
        uint8 selectedNumber;
        uint8 winningNumber;
        uint256 betAmount;
        bool isWin;
        bool isPractice;
    }

    // Mappings
    mapping(address => User) public users;
    mapping(address => Wallets) public wallets;
    mapping(address => address[]) public directReferralsList;
    mapping(address => GameResult[]) public gameHistory;
    mapping(address => uint256[]) public referralLevels; // levels unlocked

    // Practice users tracking (limit 100,000)
    uint256 public practiceUsersCount;
    uint256 public constant MAX_PRACTICE_USERS = 100000;
    uint256 public constant PRACTICE_BONUS = 100 * 10**18; // 100 USDT

    // Commission rates (in basis points, 10000 = 100%)
    uint16[] public directLevelCommission = [500, 200, 100, 100, 100, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50]; // 15 levels
    uint16[] public winnerLevelCommission = [500, 200, 100, 100, 100, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50]; // 15 levels
    uint16[] public roiCommission = [2000, 1000, 1000, 1000, 1000, 500, 500, 500, 500, 500, 300, 300, 300, 300, 300]; // 15 levels

    // Game settings
    uint8 public constant GAME_MULTIPLIER = 8;
    uint8 public constant TOTAL_NUMBERS = 8;
    uint256 public constant MIN_BET = 1 * 10**18; // 1 USDT

    // Cashback settings
    uint256 public cashbackRate = 50; // 0.5% = 50 basis points
    uint256 public constant CASHBACK_TRIGGER = 100 * 10**18; // 100 USDT losses

    // Withdrawal settings
    uint256 public constant MIN_WITHDRAWAL = 5 * 10**18;
    uint256 public constant MAX_WITHDRAWAL = 5000 * 10**18;
    uint256 public constant WITHDRAWAL_FEE = 1000; // 10%

    // Platform stats
    uint256 public totalUsers;
    uint256 public totalGamesPlayed;
    uint256 public totalVolume;

    // Randomness
    uint256 private nonce;

    // ============ Events ============

    event UserRegistered(address indexed user, address indexed referrer, uint256 timestamp);
    event Deposit(address indexed user, uint256 amount, uint256 timestamp);
    event Withdrawal(address indexed user, uint256 amount, uint256 fee, uint256 timestamp);
    event GamePlayed(address indexed user, bool isPractice, uint256 betAmount, uint8 selected, uint8 winning, bool isWin, uint256 payout);
    event ReferralCommission(address indexed from, address indexed to, uint256 amount, uint8 level, string incomeType);
    event CashbackCredited(address indexed user, uint256 amount, uint256 timestamp);
    event PracticeBalanceConverted(address indexed user, uint256 amount);

    // ============ Modifiers ============

    modifier onlyOwner() {
        require(msg.sender == owner, "Not owner");
        _;
    }

    modifier onlyRegistered() {
        require(users[msg.sender].isRegistered, "Not registered");
        _;
    }

    // ============ Constructor ============

    constructor(address _usdt) {
        usdt = IERC20(_usdt);
        owner = msg.sender;

        // Register owner as first user
        users[owner] = User({
            isRegistered: true,
            referrer: address(0),
            registrationTime: block.timestamp,
            totalDeposited: 0,
            totalWithdrawn: 0,
            directReferrals: 0,
            teamSize: 0,
            isActivated: true,
            isPremiumActivated: true,
            totalLosses: 0,
            cashbackReceived: 0,
            lastCashbackTime: block.timestamp
        });
        totalUsers = 1;
    }

    // ============ Registration ============

    /**
     * @notice Register a new user with wallet connection
     * @param _referrer The referrer's wallet address (address(0) if none)
     */
    function register(address _referrer) external {
        require(!users[msg.sender].isRegistered, "Already registered");
        require(_referrer != msg.sender, "Cannot refer yourself");

        // If referrer provided, must be registered
        if (_referrer != address(0)) {
            require(users[_referrer].isRegistered, "Referrer not registered");
        } else {
            _referrer = owner; // Default to owner
        }

        // Create user
        users[msg.sender] = User({
            isRegistered: true,
            referrer: _referrer,
            registrationTime: block.timestamp,
            totalDeposited: 0,
            totalWithdrawn: 0,
            directReferrals: 0,
            teamSize: 0,
            isActivated: false,
            isPremiumActivated: false,
            totalLosses: 0,
            cashbackReceived: 0,
            lastCashbackTime: block.timestamp
        });

        // Update referrer stats
        users[_referrer].directReferrals++;
        directReferralsList[_referrer].push(msg.sender);

        // Update team sizes up the chain
        _updateTeamSizes(_referrer);

        // Grant practice bonus if under limit
        if (practiceUsersCount < MAX_PRACTICE_USERS) {
            wallets[msg.sender].practiceBalance = PRACTICE_BONUS;
            practiceUsersCount++;

            // Distribute practice referral rewards
            _distributePracticeReferralRewards(msg.sender);
        }

        totalUsers++;

        emit UserRegistered(msg.sender, _referrer, block.timestamp);
    }

    // ============ Deposits ============

    /**
     * @notice Deposit USDT to cash balance
     * @param _amount Amount in USDT (with decimals)
     */
    function deposit(uint256 _amount) external onlyRegistered {
        require(_amount >= 10 * 10**18, "Minimum 10 USDT");
        require(usdt.transferFrom(msg.sender, address(this), _amount), "Transfer failed");

        wallets[msg.sender].cashBalance += _amount;
        users[msg.sender].totalDeposited += _amount;
        totalVolume += _amount;

        // Check activation status
        if (!users[msg.sender].isActivated && users[msg.sender].totalDeposited >= 10 * 10**18) {
            users[msg.sender].isActivated = true;
        }

        if (!users[msg.sender].isPremiumActivated && users[msg.sender].totalDeposited >= 100 * 10**18) {
            users[msg.sender].isPremiumActivated = true;
        }

        // Distribute direct level income
        _distributeDirectLevelIncome(msg.sender, _amount);

        emit Deposit(msg.sender, _amount, block.timestamp);
    }

    // ============ Game Logic ============

    /**
     * @notice Play the 8x game
     * @param _betAmount Amount to bet
     * @param _selectedNumber Number selected (1-8)
     * @param _isPractice True for practice mode
     */
    function playGame(uint256 _betAmount, uint8 _selectedNumber, bool _isPractice) external onlyRegistered {
        require(_selectedNumber >= 1 && _selectedNumber <= TOTAL_NUMBERS, "Invalid number");
        require(_betAmount >= MIN_BET, "Bet too small");

        if (_isPractice) {
            require(wallets[msg.sender].practiceBalance >= _betAmount, "Insufficient practice balance");
            wallets[msg.sender].practiceBalance -= _betAmount;
        } else {
            require(wallets[msg.sender].cashBalance >= _betAmount, "Insufficient cash balance");
            require(users[msg.sender].isActivated, "Account not activated");
            wallets[msg.sender].cashBalance -= _betAmount;
        }

        // Generate winning number (1-8)
        uint8 winningNumber = _generateRandomNumber();
        bool isWin = (_selectedNumber == winningNumber);
        uint256 payout = 0;

        if (isWin) {
            payout = _betAmount * GAME_MULTIPLIER;

            if (_isPractice) {
                // All to practice balance
                wallets[msg.sender].practiceBalance += payout;
            } else {
                // 2x to Direct Wallet (withdrawable), 6x to Cash Balance (compound)
                uint256 directPayout = _betAmount * 2;
                uint256 compoundPayout = _betAmount * 6;

                wallets[msg.sender].directWallet += directPayout;
                wallets[msg.sender].cashBalance += compoundPayout;

                // Distribute winner level income
                _distributeWinnerLevelIncome(msg.sender, payout);
            }
        } else {
            if (!_isPractice) {
                // Track losses for cashback
                users[msg.sender].totalLosses += _betAmount;
            }
            // Practice losses are burned (sent to null)
        }

        // Record game
        gameHistory[msg.sender].push(GameResult({
            timestamp: block.timestamp,
            selectedNumber: _selectedNumber,
            winningNumber: winningNumber,
            betAmount: _betAmount,
            isWin: isWin,
            isPractice: _isPractice
        }));

        totalGamesPlayed++;
        if (!_isPractice) {
            totalVolume += _betAmount;
        }

        emit GamePlayed(msg.sender, _isPractice, _betAmount, _selectedNumber, winningNumber, isWin, payout);
    }

    // ============ Withdrawals ============

    /**
     * @notice Withdraw from Direct Wallet
     * @param _amount Amount to withdraw
     */
    function withdraw(uint256 _amount) external onlyRegistered {
        require(users[msg.sender].isActivated, "Account not activated");
        require(_amount >= MIN_WITHDRAWAL, "Below minimum");
        require(_amount <= MAX_WITHDRAWAL, "Above maximum");
        require(wallets[msg.sender].directWallet >= _amount, "Insufficient balance");

        uint256 fee = (_amount * WITHDRAWAL_FEE) / 10000;
        uint256 netAmount = _amount - fee;

        wallets[msg.sender].directWallet -= _amount;
        users[msg.sender].totalWithdrawn += netAmount;

        require(usdt.transfer(msg.sender, netAmount), "Transfer failed");

        emit Withdrawal(msg.sender, netAmount, fee, block.timestamp);
    }

    // ============ Cashback ============

    /**
     * @notice Claim daily cashback (0.5% of losses)
     */
    function claimCashback() external onlyRegistered {
        require(users[msg.sender].isPremiumActivated, "Need 100+ USDT deposit");
        require(users[msg.sender].totalLosses >= CASHBACK_TRIGGER, "Losses below threshold");
        require(block.timestamp >= users[msg.sender].lastCashbackTime + 1 days, "Already claimed today");

        uint256 remainingLosses = users[msg.sender].totalLosses - users[msg.sender].cashbackReceived;
        require(remainingLosses > 0, "No losses to recover");

        uint256 cashbackAmount = (remainingLosses * cashbackRate) / 10000;

        users[msg.sender].cashbackReceived += cashbackAmount;
        users[msg.sender].lastCashbackTime = block.timestamp;
        wallets[msg.sender].directWallet += cashbackAmount;

        // Distribute ROI on ROI to uplines
        _distributeRoiOnRoi(msg.sender, cashbackAmount);

        // Auto-fund lucky draw (20% of cashback)
        uint256 luckyDrawFund = (cashbackAmount * 20) / 100;
        wallets[msg.sender].luckyDrawWallet += luckyDrawFund;
        wallets[msg.sender].directWallet -= luckyDrawFund;

        emit CashbackCredited(msg.sender, cashbackAmount, block.timestamp);
    }

    // ============ Practice to Cash Conversion ============

    /**
     * @notice Convert practice balance to cash (requires 100+ USDT deposit)
     */
    function convertPracticeToCash() external onlyRegistered {
        require(users[msg.sender].isPremiumActivated, "Need 100+ USDT deposit");
        require(wallets[msg.sender].practiceBalance > 0, "No practice balance");

        uint256 amount = wallets[msg.sender].practiceBalance;
        wallets[msg.sender].practiceBalance = 0;
        wallets[msg.sender].cashBalance += amount;

        emit PracticeBalanceConverted(msg.sender, amount);
    }

    // ============ Internal Functions ============

    function _generateRandomNumber() private returns (uint8) {
        nonce++;
        uint256 random = uint256(keccak256(abi.encodePacked(
            block.timestamp,
            block.prevrandao,
            msg.sender,
            nonce,
            totalGamesPlayed
        )));
        return uint8((random % TOTAL_NUMBERS) + 1);
    }

    function _updateTeamSizes(address _user) private {
        address current = _user;
        for (uint8 i = 0; i < 100 && current != address(0); i++) {
            users[current].teamSize++;
            current = users[current].referrer;
        }
    }

    function _distributePracticeReferralRewards(address _user) private {
        address current = users[_user].referrer;
        uint8 level = 1;

        while (current != address(0) && level <= 100) {
            uint256 reward;

            if (level == 1) {
                reward = 10 * 10**18; // 10%
            } else if (level <= 5) {
                reward = 2 * 10**18; // 2%
            } else if (level <= 10) {
                reward = 1 * 10**18; // 1%
            } else if (level <= 15) {
                reward = 5 * 10**17; // 0.5%
            } else if (level <= 50) {
                reward = 25 * 10**16; // 0.25%
            } else {
                reward = 1 * 10**17; // 0.1%
            }

            wallets[current].practiceBalance += reward;
            emit ReferralCommission(_user, current, reward, level, "PRACTICE_REFERRAL");

            current = users[current].referrer;
            level++;
        }
    }

    function _distributeDirectLevelIncome(address _user, uint256 _amount) private {
        address current = users[_user].referrer;

        for (uint8 level = 0; level < 15 && current != address(0); level++) {
            // Check if this level is unlocked for the referrer
            if (users[current].directReferrals > level && users[current].isActivated) {
                uint256 commission = (_amount * directLevelCommission[level]) / 10000;
                wallets[current].directWallet += commission;
                emit ReferralCommission(_user, current, commission, level + 1, "DIRECT_LEVEL");
            }
            current = users[current].referrer;
        }
    }

    function _distributeWinnerLevelIncome(address _user, uint256 _winAmount) private {
        address current = users[_user].referrer;

        for (uint8 level = 0; level < 15 && current != address(0); level++) {
            if (users[current].directReferrals > level && users[current].isActivated) {
                uint256 commission = (_winAmount * winnerLevelCommission[level]) / 10000;
                wallets[current].directWallet += commission;
                emit ReferralCommission(_user, current, commission, level + 1, "WINNER_LEVEL");
            }
            current = users[current].referrer;
        }
    }

    function _distributeRoiOnRoi(address _user, uint256 _cashbackAmount) private {
        address current = users[_user].referrer;
        uint256 distributableAmount = (_cashbackAmount * 50) / 100; // 50% for distribution

        for (uint8 level = 0; level < 15 && current != address(0); level++) {
            if (users[current].isActivated) {
                uint256 commission = (distributableAmount * roiCommission[level]) / 10000;
                wallets[current].directWallet += commission;
                emit ReferralCommission(_user, current, commission, level + 1, "ROI_ON_ROI");
            }
            current = users[current].referrer;
        }
    }

    // ============ View Functions ============

    function getUserInfo(address _user) external view returns (User memory) {
        return users[_user];
    }

    function getUserWallets(address _user) external view returns (Wallets memory) {
        return wallets[_user];
    }

    function getDirectReferrals(address _user) external view returns (address[] memory) {
        return directReferralsList[_user];
    }

    function getGameHistory(address _user, uint256 _limit) external view returns (GameResult[] memory) {
        uint256 totalGames = gameHistory[_user].length;
        uint256 count = _limit > totalGames ? totalGames : _limit;

        GameResult[] memory results = new GameResult[](count);
        for (uint256 i = 0; i < count; i++) {
            results[i] = gameHistory[_user][totalGames - 1 - i];
        }
        return results;
    }

    function getReferralLink(address _user) external pure returns (string memory) {
        return string(abi.encodePacked("https://trk.game/ref/", _toHexString(_user)));
    }

    function _toHexString(address _addr) private pure returns (string memory) {
        bytes memory alphabet = "0123456789abcdef";
        bytes memory data = abi.encodePacked(_addr);
        bytes memory str = new bytes(42);
        str[0] = '0';
        str[1] = 'x';
        for (uint256 i = 0; i < 20; i++) {
            str[2+i*2] = alphabet[uint8(data[i] >> 4)];
            str[3+i*2] = alphabet[uint8(data[i] & 0x0f)];
        }
        return string(str);
    }

    // ============ Admin Functions ============

    function updateCashbackRate(uint256 _newRate) external onlyOwner {
        require(_newRate <= 100, "Max 1%");
        cashbackRate = _newRate;
    }

    function emergencyWithdraw(address _token, uint256 _amount) external onlyOwner {
        IERC20(_token).transfer(owner, _amount);
    }

    function balanceOf(address account) external view returns (uint256) {
    return usdt.balanceOf(account);
}

}
