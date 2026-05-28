package com.jonvallet.chess.swiss.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TournamentAccessService {

    private final JwtService jwtService;

    public TournamentAccessService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public boolean hasAccessToTournament(UUID tournamentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        if (isAdmin) return true;

        String token = (String) auth.getCredentials();
        if (token == null) return false;

        UUID tokenTournamentId = jwtService.getTournamentId(token);
        return tournamentId.equals(tokenTournamentId);
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}
