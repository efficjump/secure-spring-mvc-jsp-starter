package com.example.webstarter.security;

import java.time.Clock;

import com.example.webstarter.user.AppUser;
import com.example.webstarter.user.AppUserRepository;
import com.example.webstarter.user.UserIdentityNormalizer;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final AppUserRepository repository;
    private final Clock clock;

    public AppUserDetailsService(AppUserRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = UserIdentityNormalizer.username(username);
        AppUser user = repository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        return toUserDetails(user);
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails userDetails, String newPassword) {
        String username = UserIdentityNormalizer.username(userDetails.getUsername());
        AppUser user = repository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new UsernameNotFoundException("User no longer exists"));
        user.updatePassword(newPassword, clock.instant());
        return toUserDetails(user);
    }

    private UserDetails toUserDetails(AppUser user) {
        String[] authorities = user.getRoles().stream()
                .map(role -> role.authority())
                .toArray(String[]::new);
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .accountLocked(!user.isAccountNonLocked(clock.instant()))
                .accountExpired(false)
                .credentialsExpired(false)
                .build();
    }
}
