package com.wasac.utilitybilling.security;

import com.wasac.utilitybilling.entity.User;
import com.wasac.utilitybilling.entity.enums.AccountStatus;
import com.wasac.utilitybilling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(user.getStatus() != AccountStatus.ACTIVE)
                .authorities(user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.name())).toList())
                .build();
    }
}
