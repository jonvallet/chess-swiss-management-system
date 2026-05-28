package com.jonvallet.chess.swiss.controller;

import com.jonvallet.chess.swiss.dto.CreateTournamentRequest;
import com.jonvallet.chess.swiss.dto.PlayerStandingDto;
import com.jonvallet.chess.swiss.dto.RegisterPlayerRequest;
import com.jonvallet.chess.swiss.dto.SubmitResultRequest;
import com.jonvallet.chess.swiss.model.Match;
import com.jonvallet.chess.swiss.model.Tournament;
import com.jonvallet.chess.swiss.model.TournamentPlayer;
import com.jonvallet.chess.swiss.model.TournamentStatus;
import com.jonvallet.chess.swiss.repository.MatchRepository;
import com.jonvallet.chess.swiss.repository.TournamentPlayerRepository;
import com.jonvallet.chess.swiss.repository.TournamentRepository;
import com.jonvallet.chess.swiss.service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TournamentController {

    private final TournamentRepository tournamentRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final MatchRepository matchRepository;
    private final TournamentService tournamentService;

    public TournamentController(TournamentRepository tournamentRepository,
                                TournamentPlayerRepository tournamentPlayerRepository,
                                MatchRepository matchRepository,
                                TournamentService tournamentService) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.matchRepository = matchRepository;
        this.tournamentService = tournamentService;
    }

    @GetMapping("/tournaments")
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        return ResponseEntity.ok(tournamentRepository.findAll());
    }

    @GetMapping("/tournaments/{id}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable UUID id) {
        return tournamentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tournaments")
    public ResponseEntity<Tournament> createTournament(@RequestBody CreateTournamentRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getTotalRounds() == null || request.getTotalRounds() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Tournament tournament = Tournament.builder()
                .name(request.getName().trim())
                .totalRounds(request.getTotalRounds())
                .currentRound(0)
                .status(TournamentStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(tournamentRepository.save(tournament));
    }

    @PostMapping("/tournaments/{id}/register")
    public ResponseEntity<TournamentPlayer> registerPlayer(
            @PathVariable UUID id,
            @RequestBody RegisterPlayerRequest request) {
        try {
            TournamentPlayer tp = tournamentService.registerPlayer(id, request.getPlayerId());
            return ResponseEntity.ok(tp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/tournaments/{id}/players")
    public ResponseEntity<List<TournamentPlayer>> getRegisteredPlayers(@PathVariable UUID id) {
        return ResponseEntity.ok(tournamentPlayerRepository.findByTournamentId(id));
    }

    @PostMapping("/tournaments/{id}/rounds/generate")
    public ResponseEntity<List<Match>> generateNextRound(@PathVariable UUID id) {
        try {
            List<Match> matches = tournamentService.generateNextRound(id);
            return ResponseEntity.ok(matches);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/tournaments/{id}/rounds/{roundNum}/matches")
    public ResponseEntity<List<Match>> getRoundMatches(
            @PathVariable UUID id,
            @PathVariable Integer roundNum) {
        return ResponseEntity.ok(matchRepository.findByTournamentIdAndRoundNumber(id, roundNum));
    }

    @GetMapping("/tournaments/{id}/matches")
    public ResponseEntity<List<Match>> getAllMatches(@PathVariable UUID id) {
        return ResponseEntity.ok(matchRepository.findByTournamentId(id));
    }

    @PostMapping("/matches/{matchId}/result")
    public ResponseEntity<Match> submitMatchResult(
            @PathVariable UUID matchId,
            @RequestBody SubmitResultRequest request) {
        try {
            Match match = tournamentService.submitMatchResult(matchId, request.getResult());
            return ResponseEntity.ok(match);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tournaments/{id}/standings")
    public ResponseEntity<List<PlayerStandingDto>> getStandings(@PathVariable UUID id) {
        try {
            List<PlayerStandingDto> standings = tournamentService.getStandings(id);
            return ResponseEntity.ok(standings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
