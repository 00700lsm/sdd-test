package com.poc.usermanagement.user;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public LoginService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("dummy-password-not-used");
    }

    public Optional<UserAccount> authenticate(String loginId, String password) {
        String normalizedId = AccountRules.normalizeId(loginId);
        UserAccount account = normalizedId.isEmpty() ? null : users.findByLoginId(normalizedId).orElse(null);
        String hash = account == null ? dummyHash : account.getPasswordHash();
        boolean matches = passwordEncoder.matches(password == null ? "" : password, hash);
        if (account == null || !matches || account.getStatus() != AccountStatus.ACTIVE) {
            return Optional.empty();
        }
        return Optional.of(account);
    }
}
