package com.poc.usermanagement.user;

import com.poc.usermanagement.web.FieldErrorCodes;
import com.poc.usermanagement.web.FieldFailure;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public List<FieldFailure> register(String loginId, String password, String name, String email) {
        String normalizedId = AccountRules.normalizeId(loginId);
        String normalizedEmail = AccountRules.normalizeEmail(email);
        String normalizedName = AccountRules.normalizeName(name);
        List<FieldFailure> errors = new ArrayList<>();
        AccountRules.addIdErrors(errors, normalizedId);
        AccountRules.addNewPasswordErrors(errors, "password", password, normalizedId);
        AccountRules.addNameErrors(errors, normalizedName);
        AccountRules.addEmailErrors(errors, normalizedEmail);
        if (!normalizedId.isEmpty() && users.existsByLoginId(normalizedId)) {
            errors.add(new FieldFailure("loginId", FieldErrorCodes.ID_DUPLICATE));
        }
        if (!normalizedEmail.isEmpty() && users.existsByEmail(normalizedEmail)) {
            errors.add(new FieldFailure("email", FieldErrorCodes.EMAIL_DUPLICATE));
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        UserAccount account = new UserAccount();
        account.setLoginId(normalizedId);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setName(normalizedName);
        account.setEmail(normalizedEmail);
        account.setRole(Role.USER);
        account.setStatus(AccountStatus.ACTIVE);
        try {
            users.save(account);
        } catch (DataIntegrityViolationException ex) {
            if (users.existsByLoginId(normalizedId)) {
                errors.add(new FieldFailure("loginId", FieldErrorCodes.ID_DUPLICATE));
            }
            if (users.existsByEmail(normalizedEmail)) {
                errors.add(new FieldFailure("email", FieldErrorCodes.EMAIL_DUPLICATE));
            }
            if (errors.isEmpty()) {
                errors.add(new FieldFailure("loginId", FieldErrorCodes.ID_DUPLICATE));
            }
        }
        return errors;
    }
}
