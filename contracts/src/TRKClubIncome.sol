// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "./IERC20.sol";

/**
 * @title TRK Club Income System
 * @notice 8% of daily turnover distributed to qualified rank holders
 */
contract TRKClubIncome {
    IERC20 public usdt;
    address public mainContract;
    address public owner;

    // Rank structure
    struct Rank {
        string name;
        uint256 targetVolume;
        uint256 poolShare; // basis points of the 8% pool
    }

    Rank[] public ranks;

    // User rank data
    struct UserRankData {
        uint256 strongLegVolume;
        uint256 otherLegsVolume;
        uint256 totalVolume;
        uint8 currentRank;
        uint256 lastClaimTime;
        uint256 totalEarned;
    }

    mapping(address => UserRankData) public userRankData;
    mapping(uint8 => address[]) public rankMembers; // rank => members

    // Daily pool
    uint256 public dailyPoolPercentage = 800; // 8% in basis points
    uint256 public todayTurnover;
    uint256 public lastResetTime;

    // Events
    event RankAchieved(address indexed user, uint8 rank, uint256 volume);
    event ClubIncomeDistributed(address indexed user, uint8 rank, uint256 amount);
    event DailyPoolReset(uint256 timestamp, uint256 previousTurnover);

    modifier onlyOwner() {
        require(msg.sender == owner, "Not owner");
        _;
    }

    modifier onlyMainContract() {
        require(msg.sender == mainContract, "Not main contract");
        _;
    }

    constructor(address _usdt) {
        usdt = IERC20(_usdt);
        owner = msg.sender;
        lastResetTime = block.timestamp;

        // Initialize ranks (50/50 rule: strong leg / other legs)
        ranks.push(Rank("Rank 1", 10000 * 10**18, 2500));      // $10,000 - 25% of 8%
        ranks.push(Rank("Rank 2", 50000 * 10**18, 2500));      // $50,000 - 25% of 8%
        ranks.push(Rank("Rank 3", 250000 * 10**18, 1250));     // $250,000 - 12.5% of 8%
        ranks.push(Rank("Rank 4", 1000000 * 10**18, 1250));    // $1,000,000 - 12.5% of 8%
        ranks.push(Rank("Rank 5", 5000000 * 10**18, 1250));    // $5,000,000 - 12.5% of 8%
        ranks.push(Rank("Rank 6", 10000000 * 10**18, 1250));   // $10,000,000 - 12.5% of 8%
    }

    /**
     * @notice Update user volume (called by main contract)
     */
    function updateUserVolume(
        address _user,
        uint256 _strongLegVolume,
        uint256 _otherLegsVolume
    ) external onlyMainContract {
        UserRankData storage data = userRankData[_user];

        data.strongLegVolume = _strongLegVolume;
        data.otherLegsVolume = _otherLegsVolume;

        // Apply 50/50 rule: min(strongLeg, otherLegs) * 2
        uint256 qualifyingVolume = _min(_strongLegVolume, _otherLegsVolume) * 2;
        data.totalVolume = qualifyingVolume;

        // Check for rank upgrade
        _updateRank(_user, qualifyingVolume);
    }

    /**
     * @notice Record turnover for daily pool
     */
    function recordTurnover(uint256 _amount) external onlyMainContract {
        // Reset if new day
        if (block.timestamp >= lastResetTime + 1 days) {
            emit DailyPoolReset(block.timestamp, todayTurnover);
            todayTurnover = 0;
            lastResetTime = block.timestamp;
        }

        todayTurnover += _amount;
    }

    /**
     * @notice Claim daily club income
     */
    function claimClubIncome() external {
        UserRankData storage data = userRankData[msg.sender];
        require(data.currentRank > 0, "No rank achieved");
        require(block.timestamp >= data.lastClaimTime + 1 days, "Already claimed today");

        // Calculate share
        uint256 dailyPool = (todayTurnover * dailyPoolPercentage) / 10000;
        uint256 rankShare = ranks[data.currentRank - 1].poolShare;
        uint256 membersInRank = rankMembers[data.currentRank].length;

        require(membersInRank > 0, "No members in rank");

        uint256 userShare = (dailyPool * rankShare) / 10000 / membersInRank;

        data.lastClaimTime = block.timestamp;
        data.totalEarned += userShare;

        require(usdt.transfer(msg.sender, userShare), "Transfer failed");

        emit ClubIncomeDistributed(msg.sender, data.currentRank, userShare);
    }

    function _updateRank(address _user, uint256 _volume) private {
        UserRankData storage data = userRankData[_user];
        uint8 oldRank = data.currentRank;
        uint8 newRank = 0;

        // Find highest qualifying rank
        for (uint8 i = 0; i < ranks.length; i++) {
            if (_volume >= ranks[i].targetVolume) {
                newRank = i + 1;
            }
        }

        if (newRank > oldRank) {
            // Remove from old rank
            if (oldRank > 0) {
                _removeFromRank(_user, oldRank);
            }

            // Add to new rank
            data.currentRank = newRank;
            rankMembers[newRank].push(_user);

            emit RankAchieved(_user, newRank, _volume);
        }
    }

    function _removeFromRank(address _user, uint8 _rank) private {
        address[] storage members = rankMembers[_rank];
        for (uint256 i = 0; i < members.length; i++) {
            if (members[i] == _user) {
                members[i] = members[members.length - 1];
                members.pop();
                break;
            }
        }
    }

    function _min(uint256 a, uint256 b) private pure returns (uint256) {
        return a < b ? a : b;
    }

    // ============ View Functions ============

    function getUserRank(address _user) external view returns (
        uint8 rank,
        string memory rankName,
        uint256 totalVolume,
        uint256 nextRankVolume
    ) {
        UserRankData storage data = userRankData[_user];
        rank = data.currentRank;

        if (rank > 0) {
            rankName = ranks[rank - 1].name;
        } else {
            rankName = "No Rank";
        }

        totalVolume = data.totalVolume;

        if (rank < ranks.length) {
            nextRankVolume = ranks[rank].targetVolume;
        } else {
            nextRankVolume = 0; // Max rank achieved
        }
    }

    function getRankMembers(uint8 _rank) external view returns (address[] memory) {
        return rankMembers[_rank];
    }

    function getDailyPoolInfo() external view returns (
        uint256 turnover,
        uint256 poolAmount,
        uint256 lastReset
    ) {
        return (
            todayTurnover,
            (todayTurnover * dailyPoolPercentage) / 10000,
            lastResetTime
        );
    }

    // ============ Admin Functions ============

    function setMainContract(address _mainContract) external onlyOwner {
        mainContract = _mainContract;
    }

    function emergencyWithdraw(address _token, uint256 _amount) external onlyOwner {
        IERC20(_token).transfer(owner, _amount);
    }
}
