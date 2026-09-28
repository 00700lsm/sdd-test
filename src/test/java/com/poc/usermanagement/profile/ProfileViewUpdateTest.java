package com.poc.usermanagement.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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

class ProfileViewUpdateTest extends AbstractWebTest {

    @Test
    void fr010_fr011_fr025_userCanViewAndUpdateOwnProfileWithoutPassword() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        mockMvc.perform(get("/me").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("member1")))
                .andExpect(content().string(containsString("회원")))
                .andExpect(content().string(containsString("member1@example.com")))
                .andExpect(content().string(containsString("USER")))
                .andExpect(content().string(containsString("ACTIVE")))
                .andExpect(content().string(not(containsString("$2"))));

        mockMvc.perform(post("/me").with(csrf()).session(session)
                        .param("name", "새이름")
                        .param("email", "member1@example.com"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/me"));
        assertThat(users.findByLoginId("member1").orElseThrow().getName()).isEqualTo("새이름");
        assertThat(users.findByLoginId("member1").orElseThrow().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void fr004_fr011_duplicateEmailAndLongNameAreReportedTogether() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("other1", USER_PASSWORD, "다른", "other1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        mockMvc.perform(post("/me").with(csrf()).session(session)
                        .param("name", "가".repeat(51))
                        .param("email", "OTHER1@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name.length")))
                .andExpect(content().string(containsString("email.duplicate")));
        assertThat(users.findByLoginId("member1").orElseThrow().getEmail()).isEqualTo("member1@example.com");
    }

    @Test
    void fr025_adminCanUpdateOwnNameAndEmail() throws Exception {
        var session = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(post("/me").with(csrf()).session(session)
                        .param("name", "새관리자")
                        .param("email", "admin2@example.com"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/me"));
        var admin = users.findByLoginId("admin").orElseThrow();
        assertThat(admin.getName()).isEqualTo("새관리자");
        assertThat(admin.getEmail()).isEqualTo("admin2@example.com");
        assertThat(admin.getLoginId()).isEqualTo("admin");
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        mockMvc.perform(get("/me").session(session))
                .andExpect(content().string(containsString("새관리자")))
                .andExpect(content().string(containsString("admin2@example.com")));
    }
}
