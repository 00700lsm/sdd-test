package com.poc.usermanagement.user;

import com.poc.usermanagement.web.FieldErrorCodes;
import com.poc.usermanagement.web.FieldFailure;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserAccountRepository users;

    public ProfileService(UserAccountRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UserAccount require(String loginId) {
        return users.findByLoginId(loginId).orElseThrow();
    }

    @Transactional
    public List<FieldFailure> update(String loginId, String name, String email) {
        UserAccount account = require(loginId);
        String normalizedName = AccountRules.normalizeName(name);
        String normalizedEmail = AccountRules.normalizeEmail(email);
        List<FieldFailure> errors = new ArrayList<>();
        AccountRules.addNameErrors(errors, normalizedName);
        AccountRules.addEmailErrors(errors, normalizedEmail);
        if (!normalizedEmail.isEmpty()
                && !normalizedEmail.equals(account.getEmail())
                && users.existsByEmailAndLoginIdNot(normalizedEmail, loginId)) {
            errors.add(new FieldFailure("email", FieldErrorCodes.EMAIL_DUPLICATE));
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        account.setName(normalizedName);
        account.setEmail(normalizedEmail);
        users.save(account);
        return errors;
    }
}
