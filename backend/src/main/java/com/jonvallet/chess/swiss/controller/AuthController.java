package com.jonvallet.chess.swiss.controller;

import com.jonvallet.chess.swiss.dto.JoinTournamentRequest;
import com.jonvallet.chess.swiss.dto.JoinTournamentResponse;
import com.jonvallet.chess.swiss.dto.LoginRequest;
import com.jonvallet.chess.swiss.dto.LoginResponse;
import com.jonvallet.chess.swiss.model.Player;
import com.jonvallet.chess.swiss.model.Tournament;
import com.jonvallet.chess.swiss.model.TournamentPlayer;
import com.jonvallet.chess.swiss.repository.PlayerRepository;
import com.jonvallet.chess.swiss.repository.TournamentRepository;
import com.jonvallet.chess.swiss.security.AppProperties;
import com.jonvallet.chess.swiss.security.JwtService;
import com.jonvallet.chess.swiss.service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AppProperties appProperties;
    private final JwtService jwtService;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TournamentService tournamentService;

    public AuthController(AppProperties appProperties,
                          JwtService jwtService,
                          TournamentRepository tournamentRepository,
                          PlayerRepository playerRepository,
                          TournamentService tournamentService) {
        this.appProperties = appProperties;
        this.jwtService = jwtService;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.tournamentService = tournamentService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String adminUsername = appProperties.getAdmin().getUsername();
        String adminPassword = appProperties.getAdmin().getPassword();

        if (adminUsername.equals(request.getUsername()) && adminPassword.equals(request.getPassword())) {
            String token = jwtService.generateAdminToken(adminUsername);
            return ResponseEntity.ok(new LoginResponse(token, "ADMIN"));
        }

        return ResponseEntity.status(401).build();
    }

    @PostMapping("/tournaments/{id}/join")
    public ResponseEntity<?> joinTournament(@PathVariable UUID id,
                                            @RequestBody JoinTournamentRequest request) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElse(null);

        if (tournament == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getPlayerName() == null || request.getPlayerName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Player name is required");
        }

        int rating = request.getPlayerRating() != null ? request.getPlayerRating() : 1200;

        Player player = new Player();
        player.setName(request.getPlayerName().trim());
        player.setRating(rating);
        player = playerRepository.save(player);

        try {
            tournamentService.registerPlayer(id, player.getId());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String token = jwtService.generatePlayerToken(player.getId(), id);

        return ResponseEntity.ok(new JoinTournamentResponse(token, player.getId(), id));
    }
}
