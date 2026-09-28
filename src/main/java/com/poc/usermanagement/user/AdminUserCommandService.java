package com.poc.usermanagement.user;

import com.poc.usermanagement.security.SessionInvalidator;
import com.poc.usermanagement.web.FieldErrorCodes;
import com.poc.usermanagement.web.FieldFailure;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserCommandService {

    private final UserAccountRepository users;
    private final SessionInvalidator sessionInvalidator;

    public AdminUserCommandService(UserAccountRepository users, SessionInvalidator sessionInvalidator) {
        this.users = users;
        this.sessionInvalidator = sessionInvalidator;
    }

    @Transactional
    public List<FieldFailure> changeStatus(String loginId, String requestedStatus) {
        UserAccount account = users.findByLoginId(AccountRules.normalizeId(loginId)).orElse(null);
        if (account == null) {
            return List.of(new FieldFailure("loginId", FieldErrorCodes.ACCOUNT_LOCKED));
        }
        if (account.getStatus() == AccountStatus.WITHDRAWN) {
            return List.of(new FieldFailure("status", FieldErrorCodes.ACCOUNT_LOCKED));
        }
        AccountStatus next = parseStatus(requestedStatus);
        if (next == null) {
            return List.of(new FieldFailure("status", FieldErrorCodes.STATUS_INVALID));
        }
        if (removesLastAdmin(account, account.getRole(), next)) {
            return List.of(new FieldFailure("status", FieldErrorCodes.ADMIN_LAST));
        }
        account.setStatus(next);
        users.save(account);
        if (next == AccountStatus.INACTIVE) {
            sessionInvalidator.invalidate(account.getLoginId());
        }
        return List.of();
    }

    @Transactional
    public List<FieldFailure> changeRole(String loginId, String requestedRole) {
        UserAccount account = users.findByLoginId(AccountRules.normalizeId(loginId)).orElse(null);
        if (account == null) {
            return List.of(new FieldFailure("loginId", FieldErrorCodes.ACCOUNT_LOCKED));
        }
        if (account.getStatus() == AccountStatus.WITHDRAWN) {
            return List.of(new FieldFailure("role", FieldErrorCodes.ACCOUNT_LOCKED));
        }
        Role next = parseRole(requestedRole);
        if (next == null) {
            return List.of(new FieldFailure("role", FieldErrorCodes.ROLE_INVALID));
        }
        if (removesLastAdmin(account, next, account.getStatus())) {
            return List.of(new FieldFailure("role", FieldErrorCodes.ADMIN_LAST));
        }
        account.setRole(next);
        users.save(account);
        return List.of();
    }

    private boolean removesLastAdmin(UserAccount account, Role nextRole, AccountStatus nextStatus) {
        if (!account.isActiveAdmin()) {
            return false;
        }
        if (nextRole == Role.ADMIN && nextStatus == AccountStatus.ACTIVE) {
            return false;
        }
        return users.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE) <= 1;
    }

    private static AccountStatus parseStatus(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("ACTIVE".equals(normalized)) {
            return AccountStatus.ACTIVE;
        }
        if ("INACTIVE".equals(normalized)) {
            return AccountStatus.INACTIVE;
        }
        return null;
    }

    private static Role parseRole(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("USER".equals(normalized)) {
            return Role.USER;
        }
        if ("ADMIN".equals(normalized)) {
            return Role.ADMIN;
        }
        return null;
    }
}
