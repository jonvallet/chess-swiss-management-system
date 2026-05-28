package com.jonvallet.chess.swiss.repository;

import com.jonvallet.chess.swiss.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {
    Optional<Tournament> findByShareCode(String shareCode);
}
