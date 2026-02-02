package com.trk.blockchain.controller;

import com.trk.blockchain.dto.ApiResponse;
import com.trk.blockchain.dto.ReferralDTO;
import com.trk.blockchain.dto.ReferralListDTO;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.security.UserDetailsImpl;
import com.trk.blockchain.service.ReferralService;
import com.trk.blockchain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referral")
public class ReferralController {

    private final ReferralService referralService;
    private final UserService userService;

    public ReferralController(ReferralService referralService, UserService userService) {
        this.referralService = referralService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ReferralDTO>> getReferralInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        ReferralDTO referralInfo = referralService.getReferralInfo(user);
        return ResponseEntity.ok(ApiResponse.success(referralInfo));
    }

    @GetMapping("/team")
    public ResponseEntity<ApiResponse<List<ReferralListDTO>>> getTeam(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        List<ReferralListDTO> referrals = referralService.getAllReferrals(user.getId())
                .stream()
                .map(ReferralListDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(referrals));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<ReferralListDTO>>> getReferralsByLevel(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable int level) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        List<ReferralListDTO> referrals = referralService.getReferralsByLevel(user.getId(), level)
                .stream()
                .map(ReferralListDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(referrals));
    }
}
