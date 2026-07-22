package com.example.webstarter.security;

import com.example.webstarter.user.UserIdentityNormalizer;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SessionRevocationService {

    private final SessionRegistry sessionRegistry;
    private final SessionMetadataStore metadataStore;

    public SessionRevocationService(SessionRegistry sessionRegistry, SessionMetadataStore metadataStore) {
        this.sessionRegistry = sessionRegistry;
        this.metadataStore = metadataStore;
    }

    public void expireAll(String rawUsername) {
        String username = UserIdentityNormalizer.username(rawUsername);
        sessionRegistry.getAllPrincipals().stream()
                .filter(UserDetails.class::isInstance)
                .map(UserDetails.class::cast)
                .filter(principal -> UserIdentityNormalizer.username(principal.getUsername()).equals(username))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .forEach(sessionInformation -> {
                    sessionInformation.expireNow();
                    metadataStore.remove(sessionInformation.getSessionId());
                });
    }
}
