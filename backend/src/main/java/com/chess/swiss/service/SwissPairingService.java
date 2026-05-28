package com.chess.swiss.service;

import com.chess.swiss.model.*;
import com.chess.swiss.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SwissPairingService {

    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final MatchRepository matchRepository;

    public SwissPairingService(TournamentPlayerRepository tournamentPlayerRepository, MatchRepository matchRepository) {
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public List<Match> generatePairings(Tournament tournament) {
        UUID tournamentId = tournament.getId();
        int nextRound = tournament.getCurrentRound() + 1;

        // 1. Fetch all registered players
        List<TournamentPlayer> enrolledPlayers = tournamentPlayerRepository.findByTournamentId(tournamentId);
        if (enrolledPlayers.isEmpty()) {
            throw new IllegalStateException("No players enrolled in this tournament.");
        }

        // 2. Fetch all previous matches to check history
        List<Match> allPastMatches = matchRepository.findByTournamentId(tournamentId);

        // Build a map of played opponents for each player
        Map<UUID, Set<UUID>> playedOpponents = new HashMap<>();
        for (TournamentPlayer tp : enrolledPlayers) {
            playedOpponents.put(tp.getPlayer().getId(), new HashSet<>());
        }
        for (Match m : allPastMatches) {
            if (m.getWhitePlayer() != null && m.getBlackPlayer() != null) {
                UUID whiteId = m.getWhitePlayer().getId();
                UUID blackId = m.getBlackPlayer().getId();
                playedOpponents.computeIfAbsent(whiteId, k -> new HashSet<>()).add(blackId);
                playedOpponents.computeIfAbsent(blackId, k -> new HashSet<>()).add(whiteId);
            }
        }

        // Build list of active players for this round
        List<TournamentPlayer> activePlayers = new ArrayList<>(enrolledPlayers);

        // 3. Handle Bye if odd number of players
        Match byeMatch = null;
        if (activePlayers.size() % 2 != 0) {
            // Find players who already had a bye
            Set<UUID> byePlayers = allPastMatches.stream()
                    .filter(Match::getIsBye)
                    .map(m -> m.getWhitePlayer() != null ? m.getWhitePlayer().getId() : m.getBlackPlayer().getId())
                    .collect(Collectors.toSet());

            // Sort players by score ascending, then rating ascending to find the weakest player who hasn't had a bye yet
            activePlayers.sort(Comparator.comparing(TournamentPlayer::getScore)
                    .thenComparing(tp -> tp.getPlayer().getRating()));

            TournamentPlayer byeRecipient = activePlayers.stream()
                    .filter(tp -> !byePlayers.contains(tp.getPlayer().getId()))
                    .findFirst()
                    .orElse(activePlayers.get(0)); // Fallback to anyone if everyone had a bye (rare)

            activePlayers.remove(byeRecipient);

            // Create Bye Match
            byeMatch = Match.builder()
                    .tournament(tournament)
                    .roundNumber(nextRound)
                    .whitePlayer(byeRecipient.getPlayer())
                    .blackPlayer(null)
                    .result(MatchResult.WHITE_WIN) // Automatic 1 point for a bye
                    .isBye(true)
                    .build();
        }

        // Sort the remaining players by score descending, then rating descending (FIDE standard seeding order)
        activePlayers.sort((a, b) -> {
            int scoreCompare = b.getScore().compareTo(a.getScore());
            if (scoreCompare != 0) return scoreCompare;
            return b.getPlayer().getRating().compareTo(a.getPlayer().getRating());
        });

        // 4. Run Backtracking Swiss Matching
        List<Pair> pairings = new ArrayList<>();
        boolean success = backtrackPairing(activePlayers, playedOpponents, 0, pairings);

        if (!success) {
            throw new IllegalStateException("Could not generate valid Swiss pairings. This usually happens if the tournament is too small for the number of rounds.");
        }

        // 5. Convert Pairings to Match Entities with Color Assignments
        List<Match> generatedMatches = new ArrayList<>();
        if (byeMatch != null) {
            generatedMatches.add(byeMatch);
        }

        for (Pair pair : pairings) {
            TournamentPlayer p1 = pair.p1;
            TournamentPlayer p2 = pair.p2;

            // Determine colors: player with lower color difference gets white (meaning they have played more black matches)
            boolean p1White = p1.getColorDifference() <= p2.getColorDifference();

            // Check history for consecutive color rules if applicable
            String p1LastColors = getPlayerLastColors(p1.getPlayer().getId(), allPastMatches);
            String p2LastColors = getPlayerLastColors(p2.getPlayer().getId(), allPastMatches);

            if (p1LastColors.endsWith("WW")) {
                p1White = false; // Forced Black
            } else if (p1LastColors.endsWith("BB")) {
                p1White = true;  // Forced White
            } else if (p2LastColors.endsWith("WW")) {
                p1White = true;  // Forced White for P1 (Forced Black for P2)
            } else if (p2LastColors.endsWith("BB")) {
                p1White = false; // Forced Black for P1 (Forced White for P2)
            }

            Match match = Match.builder()
                    .tournament(tournament)
                    .roundNumber(nextRound)
                    .whitePlayer(p1White ? p1.getPlayer() : p2.getPlayer())
                    .blackPlayer(p1White ? p2.getPlayer() : p1.getPlayer())
                    .result(MatchResult.UNPLAYED)
                    .isBye(false)
                    .build();

            generatedMatches.add(match);
        }

        return generatedMatches;
    }

    private boolean backtrackPairing(List<TournamentPlayer> players, Map<UUID, Set<UUID>> playedOpponents, int index, List<Pair> pairings) {
        // If we paired everyone, we are done
        if (index >= players.size()) {
            return true;
        }

        TournamentPlayer p1 = players.get(index);
        // If this player is already paired, move to next
        if (isAlreadyPaired(p1.getPlayer().getId(), pairings)) {
            return backtrackPairing(players, playedOpponents, index + 1, pairings);
        }

        // Find a candidate opponent p2 from the rest of the list
        for (int i = index + 1; i < players.size(); i++) {
            TournamentPlayer p2 = players.get(i);

            // Verify they aren't already paired and haven't played each other
            if (!isAlreadyPaired(p2.getPlayer().getId(), pairings) && 
                !playedOpponents.get(p1.getPlayer().getId()).contains(p2.getPlayer().getId())) {

                // Propose pair
                Pair pair = new Pair(p1, p2);
                pairings.add(pair);

                // Recurse
                if (backtrackPairing(players, playedOpponents, index + 1, pairings)) {
                    return true;
                }

                // Backtrack
                pairings.remove(pairings.size() - 1);
            }
        }

        return false;
    }

    private boolean isAlreadyPaired(UUID playerId, List<Pair> pairings) {
        for (Pair p : pairings) {
            if (p.p1.getPlayer().getId().equals(playerId) || p.p2.getPlayer().getId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    private String getPlayerLastColors(UUID playerId, List<Match> allPastMatches) {
        return allPastMatches.stream()
                .filter(m -> !m.getIsBye() && m.getResult() != MatchResult.UNPLAYED)
                .filter(m -> m.getWhitePlayer().getId().equals(playerId) || m.getBlackPlayer().getId().equals(playerId))
                .sorted(Comparator.comparing(Match::getRoundNumber))
                .map(m -> m.getWhitePlayer().getId().equals(playerId) ? "W" : "B")
                .collect(Collectors.joining());
    }

    private static class Pair {
        TournamentPlayer p1;
        TournamentPlayer p2;

        Pair(TournamentPlayer p1, TournamentPlayer p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }
}
