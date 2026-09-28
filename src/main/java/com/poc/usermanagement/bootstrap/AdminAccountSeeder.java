package com.poc.usermanagement.bootstrap;

import com.poc.usermanagement.user.AccountRules;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import com.poc.usermanagement.user.UserAccount;
import com.poc.usermanagement.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountSeeder implements ApplicationRunner {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String initialPassword;

    public AdminAccountSeeder(
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${admin.initial-password:}") String initialPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.initialPassword = initialPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByLoginId("admin")) {
            return;
        }
        if (initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalStateException("ADMIN_INITIAL_PASSWORD is required to create the initial admin account");
        }
        if (initialPassword.length() < 8 || initialPassword.length() > 64 || "admin".equals(initialPassword)) {
            throw new IllegalStateException("ADMIN_INITIAL_PASSWORD does not satisfy the password rules");
        }
        UserAccount admin = new UserAccount();
        admin.setLoginId("admin");
        admin.setPasswordHash(passwordEncoder.encode(initialPassword));
        admin.setName("관리자");
        admin.setEmail(AccountRules.normalizeEmail("admin@example.com"));
        admin.setRole(Role.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        users.save(admin);
    }
}
