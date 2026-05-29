package com.jonvallet.chess.swiss.controller;

import com.jonvallet.chess.swiss.dto.CreateAndRegisterPlayerRequest;
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
import com.jonvallet.chess.swiss.security.TournamentAccessService;
import com.jonvallet.chess.swiss.service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TournamentController {

    private final TournamentRepository tournamentRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final MatchRepository matchRepository;
    private final TournamentService tournamentService;
    private final TournamentAccessService tournamentAccessService;

    public TournamentController(TournamentRepository tournamentRepository,
                                TournamentPlayerRepository tournamentPlayerRepository,
                                MatchRepository matchRepository,
                                TournamentService tournamentService,
                                TournamentAccessService tournamentAccessService) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.matchRepository = matchRepository;
        this.tournamentService = tournamentService;
        this.tournamentAccessService = tournamentAccessService;
    }

    @GetMapping("/tournaments")
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        return ResponseEntity.ok(tournamentRepository.findAll());
    }

    @GetMapping("/tournaments/{id}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable UUID id) {
        if (!tournamentAccessService.hasReadAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        return tournamentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tournaments")
    public ResponseEntity<Tournament> createTournament(@RequestBody CreateTournamentRequest request) {
        try {
            Tournament tournament = tournamentService.createTournament(request);
            return ResponseEntity.ok(tournament);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tournaments/share/{code}")
    public ResponseEntity<Tournament> getTournamentByShareCode(@PathVariable String code) {
        try {
            Tournament tournament = tournamentService.getTournamentByShareCode(code);
            return ResponseEntity.ok(tournament);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tournaments/{id}/register")
    public ResponseEntity<TournamentPlayer> registerPlayer(
            @PathVariable UUID id,
            @RequestBody RegisterPlayerRequest request) {
        if (!tournamentAccessService.hasAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        try {
            TournamentPlayer tp = tournamentService.registerPlayer(id, request.getPlayerId());
            return ResponseEntity.ok(tp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/tournaments/{id}/players/create")
    public ResponseEntity<TournamentPlayer> createAndRegisterPlayer(
            @PathVariable UUID id,
            @RequestBody CreateAndRegisterPlayerRequest request) {
        if (!tournamentAccessService.hasAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        try {
            TournamentPlayer tp = tournamentService.createAndRegisterPlayer(id, request.getName(), request.getRating());
            return ResponseEntity.ok(tp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/tournaments/{id}")
    public ResponseEntity<?> deleteTournament(@PathVariable UUID id) {
        if (!tournamentAccessService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        tournamentService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tournaments/{id}/rounds/cancel")
    public ResponseEntity<?> cancelCurrentRound(@PathVariable UUID id) {
        if (!tournamentAccessService.hasAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        try {
            tournamentService.cancelCurrentRound(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/tournaments/{id}/players/{playerId}/bye")
    public ResponseEntity<Match> assignBye(
            @PathVariable UUID id,
            @PathVariable UUID playerId,
            @RequestBody Map<String, Integer> request) {
        if (!tournamentAccessService.hasAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        try {
            Integer roundNumber = request.get("roundNumber");
            if (roundNumber == null) {
                return ResponseEntity.badRequest().build();
            }
            Match byeMatch = tournamentService.assignBye(id, playerId, roundNumber);
            return ResponseEntity.ok(byeMatch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/tournaments/{id}/players")
    public ResponseEntity<List<TournamentPlayer>> getRegisteredPlayers(@PathVariable UUID id) {
        if (!tournamentAccessService.hasReadAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(tournamentPlayerRepository.findByTournamentId(id));
    }

    @PostMapping("/tournaments/{id}/rounds/generate")
    public ResponseEntity<List<Match>> generateNextRound(@PathVariable UUID id) {
        if (!tournamentAccessService.hasAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
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
        if (!tournamentAccessService.hasReadAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(matchRepository.findByTournamentIdAndRoundNumber(id, roundNum));
    }

    @GetMapping("/tournaments/{id}/matches")
    public ResponseEntity<List<Match>> getAllMatches(@PathVariable UUID id) {
        if (!tournamentAccessService.hasReadAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(matchRepository.findByTournamentId(id));
    }

    @PostMapping("/matches/{matchId}/result")
    public ResponseEntity<Match> submitMatchResult(
            @PathVariable UUID matchId,
            @RequestBody SubmitResultRequest request) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        if (!tournamentAccessService.hasAccessToTournament(match.getTournament().getId())) {
            return ResponseEntity.status(403).build();
        }
        try {
            Match updatedMatch = tournamentService.submitMatchResult(matchId, request.getResult());
            return ResponseEntity.ok(updatedMatch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tournaments/{id}/standings")
    public ResponseEntity<List<PlayerStandingDto>> getStandings(@PathVariable UUID id) {
        if (!tournamentAccessService.hasReadAccessToTournament(id)) {
            return ResponseEntity.status(403).build();
        }
        try {
            List<PlayerStandingDto> standings = tournamentService.getStandings(id);
            return ResponseEntity.ok(standings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
