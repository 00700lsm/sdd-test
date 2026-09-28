package com.poc.usermanagement.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PasswordChangeTest extends AbstractWebTest {

    @Test
    void fr006_fr012_fr015_userCanChangePasswordAndKeepTheSession() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("other1", USER_PASSWORD, "다른", "other1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        String otherHash = users.findByLoginId("other1").orElseThrow().getPasswordHash();
        var session = loginSession("member1", USER_PASSWORD);
        mockMvc.perform(post("/me/password").with(csrf()).session(session)
                        .param("currentPassword", USER_PASSWORD)
                        .param("newPassword", "Newpass123"))
                .andExpect(status().isFound());
        mockMvc.perform(get("/me").session(session)).andExpect(status().isOk());
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "member1")
                        .param("password", USER_PASSWORD))
                .andExpect(content().string(containsString("login.failed")));
        loginSession("member1", "Newpass123");
        assertThat(users.findByLoginId("member1").orElseThrow().getPasswordHash()).doesNotContain("Newpass123");
        assertThat(users.findByLoginId("other1").orElseThrow().getPasswordHash()).isEqualTo(otherHash);
    }

    @Test
    void fr002_fr012_wrongCurrentPasswordAndInvalidNewPasswordAreReportedTogether() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        String before = users.findByLoginId("member1").orElseThrow().getPasswordHash();
        mockMvc.perform(post("/me/password").with(csrf()).session(session)
                        .param("currentPassword", "not-the-password")
                        .param("newPassword", "short"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("password.currentMismatch")))
                .andExpect(content().string(containsString("password.length")));
        assertThat(users.findByLoginId("member1").orElseThrow().getPasswordHash()).isEqualTo(before);
    }

    @Test
    void fr012_passwordUnchangedIsRejected() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        String before = users.findByLoginId("member1").orElseThrow().getPasswordHash();
        mockMvc.perform(post("/me/password").with(csrf()).session(session)
                        .param("currentPassword", USER_PASSWORD)
                        .param("newPassword", USER_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("password.unchanged")));
        assertThat(users.findByLoginId("member1").orElseThrow().getPasswordHash()).isEqualTo(before);
    }
}
