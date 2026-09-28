package com.poc.usermanagement.security;

import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import com.poc.usermanagement.user.UserAccount;
import com.poc.usermanagement.user.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

public class AccountReloadFilter extends OncePerRequestFilter {

    private final UserAccountRepository users;
    private final SessionInvalidator sessionInvalidator;
    private final SessionRegistry sessionRegistry;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AccountReloadFilter(
            UserAccountRepository users,
            SessionInvalidator sessionInvalidator,
            SessionRegistry sessionRegistry) {
        this.users = users;
        this.sessionInvalidator = sessionInvalidator;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            SessionInformation information = sessionRegistry.getSessionInformation(session.getId());
            if (information != null && information.isExpired()) {
                clear(request, response, session);
                filterChain.doFilter(request, response);
                return;
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }
        UserAccount account = users.findByLoginId(authentication.getName()).orElse(null);
        if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
            if (account != null) {
                sessionInvalidator.invalidate(account.getLoginId());
            }
            clear(request, response, session);
            filterChain.doFilter(request, response);
            return;
        }
        if (!hasRole(authentication, account.getRole())) {
            UsernamePasswordAuthenticationToken refreshed = new UsernamePasswordAuthenticationToken(
                    account.getLoginId(),
                    null,
                    List.of(new SimpleGrantedAuthority(authority(account.getRole()))));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(refreshed);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
        }
        filterChain.doFilter(request, response);
    }

    private void clear(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        SecurityContextHolder.clearContext();
        if (session != null) {
            sessionRegistry.removeSessionInformation(session.getId());
            session.invalidate();
        }
        SecurityContext empty = SecurityContextHolder.createEmptyContext();
        securityContextRepository.saveContext(empty, request, response);
    }

    private static boolean hasRole(Authentication authentication, Role role) {
        String expected = authority(role);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (expected.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public static String authority(Role role) {
        return "ROLE_" + role.name();
    }
}
