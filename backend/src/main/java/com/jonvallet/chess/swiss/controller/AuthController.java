package com.jonvallet.chess.swiss.controller;

import com.jonvallet.chess.swiss.dto.JoinTournamentRequest;
import com.jonvallet.chess.swiss.dto.JoinTournamentResponse;
import com.jonvallet.chess.swiss.dto.LoginRequest;
import com.jonvallet.chess.swiss.dto.LoginResponse;
import com.jonvallet.chess.swiss.dto.ViewTournamentResponse;
import com.jonvallet.chess.swiss.model.Player;
import com.jonvallet.chess.swiss.model.Tournament;
import com.jonvallet.chess.swiss.repository.PlayerRepository;
import com.jonvallet.chess.swiss.repository.TournamentRepository;
import com.jonvallet.chess.swiss.security.AppProperties;
import com.jonvallet.chess.swiss.security.JwtService;
import com.jonvallet.chess.swiss.service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PostMapping("/tournaments/share/{code}/view")
    public ResponseEntity<?> viewTournament(@PathVariable String code) {
        try {
            Tournament tournament = tournamentService.getTournamentByShareCode(code.toUpperCase().trim());
            String token = jwtService.generateViewerToken(tournament.getId());
            return ResponseEntity.ok(new ViewTournamentResponse(token, tournament.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tournaments/{id}/join")
    public ResponseEntity<?> joinTournament(@PathVariable UUID id,
                                            @RequestBody JoinTournamentRequest request,
                                            Authentication authentication) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElse(null);

        if (tournament == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            if (authentication == null || !authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER"))) {
                return ResponseEntity.status(403).build();
            }

            String token = (String) authentication.getCredentials();
            UUID tokenTournamentId = jwtService.getTournamentId(token);
            if (!id.equals(tokenTournamentId)) {
                return ResponseEntity.status(403).build();
            }
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

        String playerToken = jwtService.generatePlayerToken(player.getId(), id);

        return ResponseEntity.ok(new JoinTournamentResponse(playerToken, player.getId(), id));
    }
}
