package com.poc.usermanagement.admin;

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

class AdminUserCommandTest extends AbstractWebTest {

    @Test
    void fr008_fr017_fr021_fr022_inactiveEndsTheSessionAndRoleChangeKeepsIt() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var member = loginSession("member1", USER_PASSWORD);
        var admin = loginSession("admin", ADMIN_PASSWORD);

        mockMvc.perform(post("/admin/users/member1/role").with(csrf()).session(admin).param("role", "ADMIN"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/users").session(member))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/users/member1/role").with(csrf()).session(admin).param("role", "USER"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/users").session(member))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("auth.forbidden")));
        mockMvc.perform(get("/me").session(member)).andExpect(status().isOk());

        mockMvc.perform(post("/admin/users/member1/status").with(csrf()).session(admin).param("status", "INACTIVE"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/me").session(member))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("auth.required")));
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "member1")
                        .param("password", USER_PASSWORD))
                .andExpect(content().string(containsString("login.failed")));

        mockMvc.perform(post("/admin/users/member1/status").with(csrf()).session(admin).param("status", "ACTIVE"));
        loginSession("member1", USER_PASSWORD);
        assertThat(users.findByLoginId("member1").orElseThrow().getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void fr016_fr021_fr022_fr023_withdrawnChangesAndLastAdminAreRejected() throws Exception {
        saveUser("gone1", USER_PASSWORD, "탈퇴", "gone1@example.com", Role.USER, AccountStatus.WITHDRAWN, Instant.now());
        var admin = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(post("/admin/users/gone1/status").with(csrf()).session(admin).param("status", "ACTIVE"))
                .andExpect(content().string(containsString("account.locked")));
        mockMvc.perform(post("/admin/users/gone1/role").with(csrf()).session(admin).param("role", "ADMIN"))
                .andExpect(content().string(containsString("account.locked")));
        assertThat(users.findByLoginId("gone1").orElseThrow().getStatus()).isEqualTo(AccountStatus.WITHDRAWN);

        mockMvc.perform(post("/admin/users/admin/status").with(csrf()).session(admin).param("status", "INACTIVE"))
                .andExpect(content().string(containsString("admin.last")));
        mockMvc.perform(post("/admin/users/admin/role").with(csrf()).session(admin).param("role", "USER"))
                .andExpect(content().string(containsString("admin.last")));
        assertThat(users.findByLoginId("admin").orElseThrow().isActiveAdmin()).isTrue();

        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var member = loginSession("member1", USER_PASSWORD);
        mockMvc.perform(post("/admin/users/admin/status").with(csrf()).session(member).param("status", "INACTIVE"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("auth.forbidden")));
    }

    @Test
    void fr021_withdrawnStatusRequestIsInvalid() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var admin = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(post("/admin/users/member1/status").with(csrf()).session(admin).param("status", "WITHDRAWN"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("status.invalid")));
        var member = users.findByLoginId("member1").orElseThrow();
        assertThat(member.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(member.getWithdrawnAt()).isNull();
    }
}
