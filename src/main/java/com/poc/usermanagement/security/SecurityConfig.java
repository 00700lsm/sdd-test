package com.poc.usermanagement.security;

import com.poc.usermanagement.web.FieldErrorCodes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(com.poc.usermanagement.user.UserAccountRepository users) {
        return username -> users.findByLoginId(username)
                .map(account -> org.springframework.security.core.userdetails.User.withUsername(account.getLoginId())
                        .password(account.getPasswordHash())
                        .roles(account.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("login.failed"));
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AccountReloadFilter accountReloadFilter)
            throws Exception {
        http.csrf(csrf -> { })
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/signup", "/login", "/layout.css", "/dialogs.js").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().write(FieldErrorCodes.AUTH_REQUIRED);
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().write(FieldErrorCodes.AUTH_FORBIDDEN);
                        }))
                .sessionManagement(session -> session.sessionFixation().changeSessionId())
                .addFilterAfter(accountReloadFilter, SecurityContextHolderFilter.class);
        return http.build();
    }

    @Bean
    public AccountReloadFilter accountReloadFilter(
            com.poc.usermanagement.user.UserAccountRepository users,
            SessionInvalidator sessionInvalidator,
            SessionRegistry sessionRegistry) {
        return new AccountReloadFilter(users, sessionInvalidator, sessionRegistry);
    }
}
