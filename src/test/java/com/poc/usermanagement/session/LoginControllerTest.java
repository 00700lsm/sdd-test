package com.poc.usermanagement.session;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoginControllerTest extends AbstractWebTest {

    @Test
    void fr007_fr026_activeUserAndSeededAdminCanLogIn() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "member1")
                        .param("password", USER_PASSWORD))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/me"));

        var admin = loginSession("Admin", ADMIN_PASSWORD);
        mockMvc.perform(get("/me").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin")))
                .andExpect(content().string(containsString("ADMIN")));
    }

    @Test
    void fr008_fr014_loginFailuresUseOneCode() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("inactive1", USER_PASSWORD, "중지", "inactive1@example.com", Role.USER, AccountStatus.INACTIVE, Instant.now());
        saveUser("gone1", USER_PASSWORD, "탈퇴", "gone1@example.com", Role.USER, AccountStatus.WITHDRAWN, Instant.now());

        assertLoginFailed("missing1", USER_PASSWORD);
        assertLoginFailed("member1", "wrong-password-1");
        assertLoginFailed("inactive1", USER_PASSWORD);
        assertLoginFailed("gone1", USER_PASSWORD);
    }

    @Test
    void fr007_loggedInLoginKeepsTheCurrentSession() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(post("/login").with(csrf()).session(session)
                        .param("loginId", "member1")
                        .param("password", USER_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("session.kept")));
        mockMvc.perform(get("/me").session(session))
                .andExpect(content().string(containsString("admin")));
    }

    private void assertLoginFailed(String loginId, String password) throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", loginId)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("login.failed")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("INACTIVE"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("WITHDRAWN"))));
    }
}
