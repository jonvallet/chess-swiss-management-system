package com.chess.swiss.service;

import com.chess.swiss.model.*;
import com.chess.swiss.repository.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SwissPairingServiceTest {

    @Test
    void testGeneratePairings_EvenPlayers() {
        UUID tournamentId = UUID.randomUUID();
        Tournament tournament = Tournament.builder()
                .id(tournamentId)
                .name("Test Open")
                .totalRounds(3)
                .currentRound(0)
                .status(TournamentStatus.DRAFT)
                .build();

        Player p1 = Player.builder().id(UUID.randomUUID()).name("Magnus").rating(2850).build();
        Player p2 = Player.builder().id(UUID.randomUUID()).name("Hikaru").rating(2800).build();
        Player p3 = Player.builder().id(UUID.randomUUID()).name("Fabi").rating(2780).build();
        Player p4 = Player.builder().id(UUID.randomUUID()).name("Ding").rating(2750).build();

        TournamentPlayer tp1 = TournamentPlayer.builder().tournament(tournament).player(p1).score(BigDecimal.ZERO).colorDifference(0).build();
        TournamentPlayer tp2 = TournamentPlayer.builder().tournament(tournament).player(p2).score(BigDecimal.ZERO).colorDifference(0).build();
        TournamentPlayer tp3 = TournamentPlayer.builder().tournament(tournament).player(p3).score(BigDecimal.ZERO).colorDifference(0).build();
        TournamentPlayer tp4 = TournamentPlayer.builder().tournament(tournament).player(p4).score(BigDecimal.ZERO).colorDifference(0).build();

        List<TournamentPlayer> players = Arrays.asList(tp1, tp2, tp3, tp4);

        // Standard Dynamic Proxy Stub for TournamentPlayerRepository
        TournamentPlayerRepository tpRepo = (TournamentPlayerRepository) Proxy.newProxyInstance(
                TournamentPlayerRepository.class.getClassLoader(),
                new Class<?>[]{TournamentPlayerRepository.class},
                (proxy, method, args) -> {
                    if ("findByTournamentId".equals(method.getName())) {
                        return players;
                    }
                    return null;
                }
        );

        // Standard Dynamic Proxy Stub for MatchRepository
        MatchRepository matchRepo = (MatchRepository) Proxy.newProxyInstance(
                MatchRepository.class.getClassLoader(),
                new Class<?>[]{MatchRepository.class},
                (proxy, method, args) -> {
                    if ("findByTournamentId".equals(method.getName())) {
                        return Collections.emptyList();
                    }
                    return null;
                }
        );

        SwissPairingService swissPairingService = new SwissPairingService(tpRepo, matchRepo);
        List<Match> pairings = swissPairingService.generatePairings(tournament);

        assertNotNull(pairings);
        assertEquals(2, pairings.size());

        assertFalse(pairings.get(0).getIsBye());
        assertFalse(pairings.get(1).getIsBye());

        Set<UUID> whiteIds = new HashSet<>();
        Set<UUID> blackIds = new HashSet<>();

        for (Match match : pairings) {
            whiteIds.add(match.getWhitePlayer().getId());
            blackIds.add(match.getBlackPlayer().getId());
        }

        assertEquals(2, whiteIds.size());
        assertEquals(2, blackIds.size());
        assertTrue(whiteIds.contains(p1.getId()) || blackIds.contains(p1.getId()));
        assertTrue(whiteIds.contains(p2.getId()) || blackIds.contains(p2.getId()));
        assertTrue(whiteIds.contains(p3.getId()) || blackIds.contains(p3.getId()));
        assertTrue(whiteIds.contains(p4.getId()) || blackIds.contains(p4.getId()));
    }

    @Test
    void testGeneratePairings_OddPlayers_WithBye() {
        UUID tournamentId = UUID.randomUUID();
        Tournament tournament = Tournament.builder()
                .id(tournamentId)
                .name("Test Open Odd")
                .totalRounds(3)
                .currentRound(0)
                .status(TournamentStatus.DRAFT)
                .build();

        Player p1 = Player.builder().id(UUID.randomUUID()).name("Magnus").rating(2850).build();
        Player p2 = Player.builder().id(UUID.randomUUID()).name("Hikaru").rating(2800).build();
        Player p3 = Player.builder().id(UUID.randomUUID()).name("Fabi").rating(2780).build();

        TournamentPlayer tp1 = TournamentPlayer.builder().tournament(tournament).player(p1).score(BigDecimal.ZERO).colorDifference(0).build();
        TournamentPlayer tp2 = TournamentPlayer.builder().tournament(tournament).player(p2).score(BigDecimal.ZERO).colorDifference(0).build();
        TournamentPlayer tp3 = TournamentPlayer.builder().tournament(tournament).player(p3).score(BigDecimal.ZERO).colorDifference(0).build();

        List<TournamentPlayer> players = new ArrayList<>(Arrays.asList(tp1, tp2, tp3));

        TournamentPlayerRepository tpRepo = (TournamentPlayerRepository) Proxy.newProxyInstance(
                TournamentPlayerRepository.class.getClassLoader(),
                new Class<?>[]{TournamentPlayerRepository.class},
                (proxy, method, args) -> {
                    if ("findByTournamentId".equals(method.getName())) {
                        return players;
                    }
                    return null;
                }
        );

        MatchRepository matchRepo = (MatchRepository) Proxy.newProxyInstance(
                MatchRepository.class.getClassLoader(),
                new Class<?>[]{MatchRepository.class},
                (proxy, method, args) -> {
                    if ("findByTournamentId".equals(method.getName())) {
                        return Collections.emptyList();
                    }
                    return null;
                }
        );

        SwissPairingService swissPairingService = new SwissPairingService(tpRepo, matchRepo);
        List<Match> pairings = swissPairingService.generatePairings(tournament);

        assertNotNull(pairings);
        assertEquals(2, pairings.size());

        Match byeMatch = pairings.stream().filter(Match::getIsBye).findFirst().orElse(null);
        assertNotNull(byeMatch);
        
        assertEquals(p3.getId(), byeMatch.getWhitePlayer().getId());
        assertNull(byeMatch.getBlackPlayer());
        assertEquals(MatchResult.WHITE_WIN, byeMatch.getResult());
    }
}
