package com.trk.blockchain.controller;

import com.trk.blockchain.dto.ApiResponse;
import com.trk.blockchain.dto.GameDTO;
import com.trk.blockchain.dto.GameRequest;
import com.trk.blockchain.dto.GameResponse;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.security.UserDetailsImpl;
import com.trk.blockchain.service.GameService;
import com.trk.blockchain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    public GameController(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @PostMapping("/play")
    public ResponseEntity<ApiResponse<GameResponse>> playGame(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody GameRequest request) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        GameResponse response = gameService.playGame(user, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<GameDTO>>> getGameHistory(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getCurrentUser(userDetails.getEmail());
        List<GameDTO> games = gameService.getGameHistory(user.getId())
                .stream()
                .map(GameDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(games));
    }
}
