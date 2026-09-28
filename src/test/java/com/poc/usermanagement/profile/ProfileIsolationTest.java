package com.poc.usermanagement.profile;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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

class ProfileIsolationTest extends AbstractWebTest {

    @Test
    void fr015_fr016_userCannotReadOrChangeAnotherAccount() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("other1", USER_PASSWORD, "비밀이름", "other1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);

        mockMvc.perform(get("/admin/users").session(session))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("auth.forbidden")))
                .andExpect(content().string(not(containsString("비밀이름"))));
        mockMvc.perform(get("/admin/users/other1").session(session))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString("other1@example.com"))));
        mockMvc.perform(post("/admin/users/other1/status").with(csrf()).session(session).param("status", "INACTIVE"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/users/other1/role").with(csrf()).session(session).param("role", "ADMIN"))
                .andExpect(status().isForbidden());
    }
}
