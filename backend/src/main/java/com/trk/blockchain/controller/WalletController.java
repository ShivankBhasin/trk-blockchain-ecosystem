package com.trk.blockchain.controller;

import com.trk.blockchain.dto.*;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.security.UserDetailsImpl;
import com.trk.blockchain.service.UserService;
import com.trk.blockchain.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    public WalletController(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WalletDTO>> getWallet(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        WalletDTO wallet = walletService.getWalletDTO(user);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<WalletDTO>> deposit(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody DepositRequest request) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        WalletDTO wallet = walletService.deposit(user, request);
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", wallet));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WalletDTO>> withdraw(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody WithdrawRequest request) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        WalletDTO wallet = walletService.withdraw(user, request);
        return ResponseEntity.ok(ApiResponse.success("Withdrawal initiated", wallet));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<WalletDTO>> transfer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody TransferRequest request) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        WalletDTO wallet = walletService.transfer(user, request);
        return ResponseEntity.ok(ApiResponse.success("Transfer successful", wallet));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactions(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        List<TransactionDTO> transactions = walletService.getTransactionHistory(user.getId())
                .stream()
                .map(TransactionDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
