package com.jonvallet.chess.swiss.repository;

import com.jonvallet.chess.swiss.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByTournamentId(UUID tournamentId);
    List<Match> findByTournamentIdAndRoundNumber(UUID tournamentId, Integer roundNumber);
}
