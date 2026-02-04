// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "./IERC20.sol";

/**
 * @title TRK Lucky Draw System
 * @notice Decentralized lottery with 10,000 tickets, 1,000 winners, 70,000 USDT prize pool
 */
contract TRKLuckyDraw {
    IERC20 public usdt;
    address public mainContract;
    address public owner;

    // Draw configuration
    uint256 public constant TICKET_PRICE = 10 * 10**18; // 10 USDT
    uint256 public constant TOTAL_TICKETS = 10000;
    uint256 public constant TOTAL_WINNERS = 1000;
    uint256 public constant PRIZE_POOL = 70000 * 10**18; // 70,000 USDT

    // Prize distribution
    uint256[] public prizeAmounts = [
        10000 * 10**18,  // 1st: 10,000 USDT
        5000 * 10**18,   // 2nd: 5,000 USDT
        4000 * 10**18,   // 3rd: 4,000 USDT
        1000 * 10**18,   // 4th-10th: 1,000 USDT each (7 winners)
        300 * 10**18,    // 11th-50th: 300 USDT each (40 winners)
        120 * 10**18,    // 51st-100th: 120 USDT each (50 winners)
        40 * 10**18,     // 101st-500th: 40 USDT each (400 winners)
        20 * 10**18      // 501st-1000th: 20 USDT each (500 winners)
    ];

    uint256[] public winnersPerTier = [1, 1, 1, 7, 40, 50, 400, 500];

    // Draw state
    struct Draw {
        uint256 drawId;
        uint256 startTime;
        uint256 endTime;
        uint256 ticketsSold;
        bool isComplete;
        address[] participants;
        address[] winners;
    }

    uint256 public currentDrawId;
    mapping(uint256 => Draw) public draws;
    mapping(uint256 => mapping(address => uint256)) public userTickets; // drawId => user => tickets
    mapping(uint256 => mapping(uint256 => address)) public ticketOwners; // drawId => ticketNumber => owner

    // Events
    event TicketPurchased(address indexed user, uint256 drawId, uint256 ticketCount, uint256 totalCost);
    event DrawStarted(uint256 indexed drawId, uint256 startTime);
    event DrawCompleted(uint256 indexed drawId, uint256 endTime);
    event PrizeAwarded(uint256 indexed drawId, address indexed winner, uint256 rank, uint256 amount);

    modifier onlyOwner() {
        require(msg.sender == owner, "Not owner");
        _;
    }

    constructor(address _usdt) {
        usdt = IERC20(_usdt);
        owner = msg.sender;
        _startNewDraw();
    }

    /**
     * @notice Buy lottery tickets
     * @param _quantity Number of tickets to buy
     */
    function buyTickets(uint256 _quantity) external {
        require(_quantity > 0 && _quantity <= 100, "Invalid quantity");

        Draw storage draw = draws[currentDrawId];
        require(!draw.isComplete, "Draw completed");
        require(draw.ticketsSold + _quantity <= TOTAL_TICKETS, "Not enough tickets");

        uint256 totalCost = TICKET_PRICE * _quantity;
        require(usdt.transferFrom(msg.sender, address(this), totalCost), "Payment failed");

        // Assign tickets
        for (uint256 i = 0; i < _quantity; i++) {
            uint256 ticketNumber = draw.ticketsSold + i + 1;
            ticketOwners[currentDrawId][ticketNumber] = msg.sender;
        }

        // Update user tickets
        if (userTickets[currentDrawId][msg.sender] == 0) {
            draw.participants.push(msg.sender);
        }
        userTickets[currentDrawId][msg.sender] += _quantity;
        draw.ticketsSold += _quantity;

        emit TicketPurchased(msg.sender, currentDrawId, _quantity, totalCost);

        // Auto-trigger draw if all tickets sold
        if (draw.ticketsSold == TOTAL_TICKETS) {
            _executeDraw();
        }
    }

    /**
     * @notice Buy tickets from Lucky Draw Wallet (called by main contract)
     */
    function buyTicketsFromWallet(address _user, uint256 _quantity) external {
        require(msg.sender == mainContract, "Only main contract");
        require(_quantity > 0, "Invalid quantity");

        Draw storage draw = draws[currentDrawId];
        require(!draw.isComplete, "Draw completed");
        require(draw.ticketsSold + _quantity <= TOTAL_TICKETS, "Not enough tickets");

        // Assign tickets
        for (uint256 i = 0; i < _quantity; i++) {
            uint256 ticketNumber = draw.ticketsSold + i + 1;
            ticketOwners[currentDrawId][ticketNumber] = _user;
        }

        if (userTickets[currentDrawId][_user] == 0) {
            draw.participants.push(_user);
        }
        userTickets[currentDrawId][_user] += _quantity;
        draw.ticketsSold += _quantity;

        emit TicketPurchased(_user, currentDrawId, _quantity, TICKET_PRICE * _quantity);

        if (draw.ticketsSold == TOTAL_TICKETS) {
            _executeDraw();
        }
    }

    /**
     * @notice Execute the draw (internal)
     */
    function _executeDraw() private {
        Draw storage draw = draws[currentDrawId];
        require(draw.ticketsSold == TOTAL_TICKETS, "Tickets not sold out");
        require(!draw.isComplete, "Already executed");

        // Generate random winners
        uint256[] memory winningTickets = _generateWinningTickets();

        // Distribute prizes
        uint256 winnerIndex = 0;
        uint256 tierIndex = 0;
        uint256 winnersInCurrentTier = 0;

        for (uint256 i = 0; i < TOTAL_WINNERS; i++) {
            address winner = ticketOwners[currentDrawId][winningTickets[i]];
            draw.winners.push(winner);

            // Determine prize amount based on tier
            uint256 prizeAmount = prizeAmounts[tierIndex];

            // Transfer prize
            require(usdt.transfer(winner, prizeAmount), "Prize transfer failed");

            emit PrizeAwarded(currentDrawId, winner, i + 1, prizeAmount);

            winnersInCurrentTier++;
            if (winnersInCurrentTier >= winnersPerTier[tierIndex] && tierIndex < prizeAmounts.length - 1) {
                tierIndex++;
                winnersInCurrentTier = 0;
            }
            winnerIndex++;
        }

        draw.isComplete = true;
        draw.endTime = block.timestamp;

        emit DrawCompleted(currentDrawId, block.timestamp);

        // Start new draw
        _startNewDraw();
    }

    /**
     * @notice Generate random winning ticket numbers
     */
    function _generateWinningTickets() private view returns (uint256[] memory) {
        uint256[] memory winners = new uint256[](TOTAL_WINNERS);
        bool[] memory selected = new bool[](TOTAL_TICKETS + 1);

        uint256 count = 0;
        uint256 nonce = 0;

        while (count < TOTAL_WINNERS) {
            nonce++;
            uint256 random = uint256(keccak256(abi.encodePacked(
                block.timestamp,
                block.prevrandao,
                currentDrawId,
                nonce,
                count
            )));

            uint256 ticketNumber = (random % TOTAL_TICKETS) + 1;

            if (!selected[ticketNumber]) {
                selected[ticketNumber] = true;
                winners[count] = ticketNumber;
                count++;
            }
        }

        return winners;
    }

    function _startNewDraw() private {
        currentDrawId++;
        draws[currentDrawId] = Draw({
            drawId: currentDrawId,
            startTime: block.timestamp,
            endTime: 0,
            ticketsSold: 0,
            isComplete: false,
            participants: new address[](0),
            winners: new address[](0)
        });

        emit DrawStarted(currentDrawId, block.timestamp);
    }

    // ============ View Functions ============

    function getCurrentDraw() external view returns (
        uint256 drawId,
        uint256 ticketsSold,
        uint256 ticketsRemaining,
        uint256 startTime,
        bool isComplete
    ) {
        Draw storage draw = draws[currentDrawId];
        return (
            draw.drawId,
            draw.ticketsSold,
            TOTAL_TICKETS - draw.ticketsSold,
            draw.startTime,
            draw.isComplete
        );
    }

    function getUserTickets(uint256 _drawId, address _user) external view returns (uint256) {
        return userTickets[_drawId][_user];
    }

    function getDrawWinners(uint256 _drawId) external view returns (address[] memory) {
        return draws[_drawId].winners;
    }

    function getDrawParticipants(uint256 _drawId) external view returns (address[] memory) {
        return draws[_drawId].participants;
    }

    // ============ Admin Functions ============

    function setMainContract(address _mainContract) external onlyOwner {
        mainContract = _mainContract;
    }

    function emergencyWithdraw(address _token, uint256 _amount) external onlyOwner {
        IERC20(_token).transfer(owner, _amount);
    }
}
