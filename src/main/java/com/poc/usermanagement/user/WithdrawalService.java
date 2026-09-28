package com.poc.usermanagement.user;

import com.poc.usermanagement.security.SessionInvalidator;
import com.poc.usermanagement.web.FieldErrorCodes;
import com.poc.usermanagement.web.FieldFailure;
import java.time.Instant;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WithdrawalService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SessionInvalidator sessionInvalidator;

    public WithdrawalService(
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            SessionInvalidator sessionInvalidator) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.sessionInvalidator = sessionInvalidator;
    }

    @Transactional
    public List<FieldFailure> withdraw(String loginId, String currentPassword) {
        UserAccount account = users.findByLoginId(loginId).orElseThrow();
        String current = currentPassword == null ? "" : currentPassword;
        if (current.isEmpty() || !passwordEncoder.matches(current, account.getPasswordHash())) {
            return List.of(new FieldFailure("currentPassword", FieldErrorCodes.PASSWORD_CURRENT_MISMATCH));
        }
        if (account.isActiveAdmin() && users.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE) <= 1) {
            return List.of(new FieldFailure("account", FieldErrorCodes.ADMIN_LAST));
        }
        account.setStatus(AccountStatus.WITHDRAWN);
        account.setWithdrawnAt(Instant.now());
        users.save(account);
        sessionInvalidator.invalidate(loginId);
        return List.of();
    }
}
