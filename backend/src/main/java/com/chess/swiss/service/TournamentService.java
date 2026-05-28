package com.chess.swiss.service;

import com.chess.swiss.dto.PlayerStandingDto;
import com.chess.swiss.model.*;
import com.chess.swiss.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final MatchRepository matchRepository;
    private final SwissPairingService swissPairingService;

    public TournamentService(TournamentRepository tournamentRepository,
                             PlayerRepository playerRepository,
                             TournamentPlayerRepository tournamentPlayerRepository,
                             MatchRepository matchRepository,
                             SwissPairingService swissPairingService) {
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.matchRepository = matchRepository;
        this.swissPairingService = swissPairingService;
    }

    @Transactional
    public TournamentPlayer registerPlayer(UUID tournamentId, UUID playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new IllegalStateException("Players can only be registered while tournament is in DRAFT status");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        TournamentPlayerId id = new TournamentPlayerId(tournamentId, playerId);
        if (tournamentPlayerRepository.existsById(id)) {
            throw new IllegalStateException("Player is already registered for this tournament");
        }

        TournamentPlayer tp = TournamentPlayer.builder()
                .id(id)
                .tournament(tournament)
                .player(player)
                .score(BigDecimal.ZERO)
                .colorDifference(0)
                .build();

        return tournamentPlayerRepository.save(tp);
    }

    @Transactional
    public List<Match> generateNextRound(UUID tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (tournament.getStatus() == TournamentStatus.FINISHED) {
            throw new IllegalStateException("Tournament is already finished.");
        }

        if (tournament.getCurrentRound() >= tournament.getTotalRounds()) {
            throw new IllegalStateException("All rounds have already been completed.");
        }

        // Verify all matches of the current round are completed (unplayed is not allowed)
        if (tournament.getCurrentRound() > 0) {
            List<Match> currentRoundMatches = matchRepository.findByTournamentIdAndRoundNumber(
                    tournamentId, tournament.getCurrentRound());
            boolean hasUnplayed = currentRoundMatches.stream()
                    .anyMatch(m -> m.getResult() == MatchResult.UNPLAYED);
            if (hasUnplayed) {
                throw new IllegalStateException("Cannot generate next round. Some matches in the current round are still unplayed.");
            }
        }

        // Generate pairings
        List<Match> newMatches = swissPairingService.generatePairings(tournament);

        // Save generated matches
        newMatches = matchRepository.saveAll(newMatches);

        // Update tournament state
        tournament.setCurrentRound(tournament.getCurrentRound() + 1);
        if (tournament.getStatus() == TournamentStatus.DRAFT) {
            tournament.setStatus(TournamentStatus.IN_PROGRESS);
        }
        tournamentRepository.save(tournament);

        return newMatches;
    }

    @Transactional
    public Match submitMatchResult(UUID matchId, MatchResult result) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        if (match.getResult() != MatchResult.UNPLAYED) {
            // Rollback previous scores if result is being updated/edited
            revertMatchScores(match);
        }

        match.setResult(result);

        if (!match.getIsBye()) {
            TournamentPlayer whiteTP = tournamentPlayerRepository.findById(
                    new TournamentPlayerId(match.getTournament().getId(), match.getWhitePlayer().getId()))
                    .orElseThrow(() -> new IllegalStateException("White player registration record missing"));

            TournamentPlayer blackTP = tournamentPlayerRepository.findById(
                    new TournamentPlayerId(match.getTournament().getId(), match.getBlackPlayer().getId()))
                    .orElseThrow(() -> new IllegalStateException("Black player registration record missing"));

            // Update scores & color differences
            if (result == MatchResult.WHITE_WIN) {
                whiteTP.setScore(whiteTP.getScore().add(BigDecimal.ONE));
            } else if (result == MatchResult.BLACK_WIN) {
                blackTP.setScore(blackTP.getScore().add(BigDecimal.ONE));
            } else if (result == MatchResult.DRAW) {
                whiteTP.setScore(whiteTP.getScore().add(new BigDecimal("0.5")));
                blackTP.setScore(blackTP.getScore().add(new BigDecimal("0.5")));
            }

            whiteTP.setColorDifference(whiteTP.getColorDifference() + 1);
            blackTP.setColorDifference(blackTP.getColorDifference() - 1);

            tournamentPlayerRepository.save(whiteTP);
            tournamentPlayerRepository.save(blackTP);
        } else {
            // Bye matches automatically award a point when created, but if we need to enforce score explicitly:
            TournamentPlayer p = tournamentPlayerRepository.findById(
                    new TournamentPlayerId(match.getTournament().getId(), match.getWhitePlayer().getId()))
                    .orElseThrow(() -> new IllegalStateException("Player registration record missing"));
            
            p.setScore(p.getScore().add(BigDecimal.ONE));
            tournamentPlayerRepository.save(p);
        }

        match = matchRepository.save(match);

        // Check if tournament should be finished
        checkAndFinishTournament(match.getTournament());

        return match;
    }

    private void revertMatchScores(Match match) {
        MatchResult oldResult = match.getResult();
        if (oldResult == MatchResult.UNPLAYED) return;

        if (!match.getIsBye()) {
            TournamentPlayer whiteTP = tournamentPlayerRepository.findById(
                    new TournamentPlayerId(match.getTournament().getId(), match.getWhitePlayer().getId())).orElse(null);
            TournamentPlayer blackTP = tournamentPlayerRepository.findById(
                    new TournamentPlayerId(match.getTournament().getId(), match.getBlackPlayer().getId())).orElse(null);

            if (whiteTP != null && blackTP != null) {
                if (oldResult == MatchResult.WHITE_WIN) {
                    whiteTP.setScore(whiteTP.getScore().subtract(BigDecimal.ONE));
                } else if (oldResult == MatchResult.BLACK_WIN) {
                    blackTP.setScore(blackTP.getScore().subtract(BigDecimal.ONE));
                } else if (oldResult == MatchResult.DRAW) {
                    whiteTP.setScore(whiteTP.getScore().subtract(new BigDecimal("0.5")));
                    blackTP.setScore(blackTP.getScore().subtract(new BigDecimal("0.5")));
                }
                whiteTP.setColorDifference(whiteTP.getColorDifference() - 1);
                blackTP.setColorDifference(blackTP.getColorDifference() + 1);
                tournamentPlayerRepository.save(whiteTP);
                tournamentPlayerRepository.save(blackTP);
            }
        } else {
            TournamentPlayer p = tournamentPlayerRepository.findById(
                    new TournamentPlayerId(match.getTournament().getId(), match.getWhitePlayer().getId())).orElse(null);
            if (p != null) {
                p.setScore(p.getScore().subtract(BigDecimal.ONE));
                tournamentPlayerRepository.save(p);
            }
        }
    }

    private void checkAndFinishTournament(Tournament tournament) {
        if (tournament.getCurrentRound().equals(tournament.getTotalRounds())) {
            List<Match> allMatches = matchRepository.findByTournamentId(tournament.getId());
            boolean allFinished = allMatches.stream().allMatch(m -> m.getResult() != MatchResult.UNPLAYED);
            if (allFinished) {
                tournament.setStatus(TournamentStatus.FINISHED);
                tournamentRepository.save(tournament);
            }
        }
    }

    public List<PlayerStandingDto> getStandings(UUID tournamentId) {
        List<TournamentPlayer> players = tournamentPlayerRepository.findByTournamentId(tournamentId);
        List<Match> allMatches = matchRepository.findByTournamentId(tournamentId);

        // Build player scores map for quick Buchholz computation
        Map<UUID, BigDecimal> scores = players.stream()
                .collect(Collectors.toMap(tp -> tp.getPlayer().getId(), TournamentPlayer::getScore));

        // Build list of opponents for each player
        Map<UUID, List<UUID>> opponents = new HashMap<>();
        for (TournamentPlayer tp : players) {
            opponents.put(tp.getPlayer().getId(), new ArrayList<>());
        }
        for (Match m : allMatches) {
            if (m.getWhitePlayer() != null && m.getBlackPlayer() != null) {
                UUID wId = m.getWhitePlayer().getId();
                UUID bId = m.getBlackPlayer().getId();
                opponents.computeIfAbsent(wId, k -> new ArrayList<>()).add(bId);
                opponents.computeIfAbsent(bId, k -> new ArrayList<>()).add(wId);
            }
        }

        // Map standing dto
        List<PlayerStandingDto> standings = players.stream().map(tp -> {
            UUID pId = tp.getPlayer().getId();
            
            // Buchholz = sum of opponents' scores
            BigDecimal buchholz = opponents.get(pId).stream()
                    .map(oppId -> scores.getOrDefault(oppId, BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return PlayerStandingDto.builder()
                    .playerId(pId)
                    .playerName(tp.getPlayer().getName())
                    .rating(tp.getPlayer().getRating())
                    .score(tp.getScore())
                    .buchholz(buchholz)
                    .colorDifference(tp.getColorDifference())
                    .build();
        }).collect(Collectors.toList());

        // Sort: Score desc, Buchholz desc, Rating desc
        standings.sort((a, b) -> {
            int scoreCompare = b.getScore().compareTo(a.getScore());
            if (scoreCompare != 0) return scoreCompare;
            int bhCompare = b.getBuchholz().compareTo(a.getBuchholz());
            if (bhCompare != 0) return bhCompare;
            return b.getRating().compareTo(a.getRating());
        });

        // Assign ranks
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setRank(i + 1);
        }

        return standings;
    }
}
