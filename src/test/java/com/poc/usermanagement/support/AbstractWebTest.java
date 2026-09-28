package com.poc.usermanagement.support;

import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import com.poc.usermanagement.user.UserAccount;
import com.poc.usermanagement.user.UserAccountRepository;
import java.security.SecureRandom;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractWebTest {

    protected static final String ADMIN_PASSWORD = randomAdminPassword();
    protected static final String USER_PASSWORD = "Userpass1";

    @DynamicPropertySource
    static void initialAdminPassword(DynamicPropertyRegistry registry) {
        registry.add("admin.initial-password", () -> ADMIN_PASSWORD);
    }

    private static String randomAdminPassword() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        StringBuilder password = new StringBuilder();
        for (byte value : bytes) {
            password.append((char) ('a' + Math.floorMod(value, 26)));
        }
        return password.toString();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserAccountRepository users;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected MockHttpSession loginSession(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", loginId)
                        .param("password", password))
                .andExpect(status().isFound())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    protected UserAccount saveUser(
            String loginId,
            String rawPassword,
            String name,
            String email,
            Role role,
            AccountStatus status,
            Instant createdAt) {
        UserAccount account = new UserAccount();
        account.setLoginId(loginId);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setName(name);
        account.setEmail(email);
        account.setRole(role);
        account.setStatus(status);
        account.setCreatedAt(createdAt);
        if (status == AccountStatus.WITHDRAWN) {
            account.setWithdrawnAt(Instant.parse("2026-01-01T00:00:00Z"));
        }
        return users.save(account);
    }
}
