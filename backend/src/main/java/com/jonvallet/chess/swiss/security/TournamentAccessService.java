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

        if (hasRole(auth, "ROLE_ADMIN")) return true;

        if (!hasRole(auth, "ROLE_PLAYER")) return false;

        return tournamentIdMatches(auth, tournamentId);
    }

    public boolean hasReadAccessToTournament(UUID tournamentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        if (hasRole(auth, "ROLE_ADMIN")) return true;

        if (!hasRole(auth, "ROLE_PLAYER") && !hasRole(auth, "ROLE_VIEWER")) return false;

        return tournamentIdMatches(auth, tournamentId);
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        return hasRole(auth, "ROLE_ADMIN");
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(role));
    }

    private boolean tournamentIdMatches(Authentication auth, UUID tournamentId) {
        String token = (String) auth.getCredentials();
        if (token == null) return false;

        UUID tokenTournamentId = jwtService.getTournamentId(token);
        return tournamentId.equals(tokenTournamentId);
    }
}
