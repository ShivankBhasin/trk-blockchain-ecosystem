package com.trk.blockchain.controller;

import com.trk.blockchain.dto.ApiResponse;
import com.trk.blockchain.dto.DashboardDTO;
import com.trk.blockchain.dto.UserDTO;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.security.UserDetailsImpl;
import com.trk.blockchain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        UserDTO userDTO = userService.getUserDTO(user);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        DashboardDTO dashboard = userService.getDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
