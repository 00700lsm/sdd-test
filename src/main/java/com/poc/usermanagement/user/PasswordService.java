package com.poc.usermanagement.user;

import com.poc.usermanagement.web.FieldErrorCodes;
import com.poc.usermanagement.web.FieldFailure;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public List<FieldFailure> change(String loginId, String currentPassword, String newPassword) {
        UserAccount account = users.findByLoginId(loginId).orElseThrow();
        List<FieldFailure> errors = new ArrayList<>();
        String current = currentPassword == null ? "" : currentPassword;
        if (current.isEmpty() || !passwordEncoder.matches(current, account.getPasswordHash())) {
            errors.add(new FieldFailure("currentPassword", FieldErrorCodes.PASSWORD_CURRENT_MISMATCH));
        }
        AccountRules.addNewPasswordErrors(errors, "newPassword", newPassword, account.getLoginId());
        if (newPassword != null && !newPassword.isEmpty() && passwordEncoder.matches(newPassword, account.getPasswordHash())) {
            errors.add(new FieldFailure("newPassword", FieldErrorCodes.PASSWORD_UNCHANGED));
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        users.save(account);
        return errors;
    }
}
