package com.trk.blockchain.controller;

import com.trk.blockchain.dto.ApiResponse;
import com.trk.blockchain.dto.LuckyDrawDTO;
import com.trk.blockchain.entity.LuckyDraw;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.security.UserDetailsImpl;
import com.trk.blockchain.service.LuckyDrawService;
import com.trk.blockchain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lucky-draw")
@RequiredArgsConstructor
public class LuckyDrawController {

    private final LuckyDrawService luckyDrawService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<LuckyDrawDTO>> getCurrentDraw(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        LuckyDrawDTO drawDTO = luckyDrawService.getCurrentDraw(user);
        return ResponseEntity.ok(ApiResponse.success(drawDTO));
    }

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<LuckyDrawDTO>> buyTicket(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "1") int quantity) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        LuckyDrawDTO drawDTO = luckyDrawService.buyTicket(user, quantity);
        return ResponseEntity.ok(ApiResponse.success("Ticket(s) purchased successfully", drawDTO));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<LuckyDraw>>> getDrawHistory() {
        List<LuckyDraw> draws = luckyDrawService.getDrawHistory();
        return ResponseEntity.ok(ApiResponse.success(draws));
    }
}
