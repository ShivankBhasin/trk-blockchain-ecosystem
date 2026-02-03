package com.trk.blockchain.controller;

import com.trk.blockchain.dto.ApiResponse;
import com.trk.blockchain.dto.IncomeDTO;
import com.trk.blockchain.entity.Income;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.security.UserDetailsImpl;
import com.trk.blockchain.service.IncomeService;
import com.trk.blockchain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<IncomeDTO>> getIncomeOverview(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        IncomeDTO incomeDTO = incomeService.getIncomeOverview(user);
        return ResponseEntity.ok(ApiResponse.success(incomeDTO));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Income>>> getIncomeHistory(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        List<Income> incomes = incomeService.getIncomeHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success(incomes));
    }
}
