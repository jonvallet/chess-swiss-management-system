package com.jonvallet.chess.swiss.repository;

import com.jonvallet.chess.swiss.model.TournamentPlayer;
import com.jonvallet.chess.swiss.model.TournamentPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentPlayerRepository extends JpaRepository<TournamentPlayer, TournamentPlayerId> {
    List<TournamentPlayer> findByTournamentId(UUID tournamentId);
}
