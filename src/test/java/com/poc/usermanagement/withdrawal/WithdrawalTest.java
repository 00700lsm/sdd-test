package com.poc.usermanagement.withdrawal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import com.poc.usermanagement.user.UserAccount;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class WithdrawalTest extends AbstractWebTest {

    @Test
    void fr013_fr014_fr024_withdrawalKeepsTheRowAndBlocksLogin() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        long before = users.findByLoginId("member1").stream().count() + users.findByLoginId("admin").stream().count();
        var session = loginSession("member1", USER_PASSWORD);
        mockMvc.perform(post("/me/withdrawal").with(csrf()).session(session)
                        .param("currentPassword", USER_PASSWORD))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
        UserAccount withdrawn = users.findByLoginId("member1").orElseThrow();
        assertThat(withdrawn.getStatus()).isEqualTo(AccountStatus.WITHDRAWN);
        assertThat(withdrawn.getWithdrawnAt()).isNotNull();
        assertThat(users.findByLoginId("admin")).isPresent();
        assertThat(before).isEqualTo(2);
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "member1")
                        .param("password", USER_PASSWORD))
                .andExpect(content().string(containsString("login.failed")));
    }

    @Test
    void fr013_wrongPasswordDoesNotWithdraw() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        mockMvc.perform(post("/me/withdrawal").with(csrf()).session(session)
                        .param("currentPassword", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("password.currentMismatch")));
        UserAccount account = users.findByLoginId("member1").orElseThrow();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getWithdrawnAt()).isNull();
    }

    @Test
    void fr023_fr025_lastActiveAdminCannotWithdraw() throws Exception {
        var session = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(post("/me/withdrawal").with(csrf()).session(session)
                        .param("currentPassword", ADMIN_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin.last")));
        assertThat(users.findByLoginId("admin").orElseThrow().getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }
}
