package com.poc.usermanagement.signup;

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
import com.poc.usermanagement.user.UserAccount;
import org.junit.jupiter.api.Test;

class SignupControllerTest extends AbstractWebTest {

    @Test
    void fr001_fr005_fr006_signupCreatesActiveUserWithoutPlainPassword() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "Abcd")
                        .param("password", "Secretpass1")
                        .param("name", " 홍길동 ")
                        .param("email", " NewUser@Example.com "))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(content().string(not(containsString("Secretpass1"))));

        UserAccount saved = users.findByLoginId("abcd").orElseThrow();
        assertThat(saved.getRole().name()).isEqualTo("USER");
        assertThat(saved.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(saved.getName()).isEqualTo("홍길동");
        assertThat(saved.getEmail()).isEqualTo("newuser@example.com");
        assertThat(saved.getPasswordHash()).doesNotContain("Secretpass1");
        assertThat(saved.getPasswordHash()).startsWith("$2");
    }

    @Test
    void fr002_fr003_fr004_duplicateIdAndEmailAreReportedTogether() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "taken1")
                        .param("password", "Secretpass1")
                        .param("name", "기존")
                        .param("email", "taken@example.com"))
                .andExpect(status().isFound());

        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "TAKEN1")
                        .param("password", "Secretpass1")
                        .param("name", "다른")
                        .param("email", "Taken@Example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id.duplicate")))
                .andExpect(content().string(containsString("email.duplicate")));

        assertThat(users.findByLoginId("taken1")).isPresent();
        assertThat(users.findByLoginId("다른")).isEmpty();
    }

    @Test
    void fr002_multipleFieldFailuresAreReturnedTogether() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "ab")
                        .param("password", "short")
                        .param("name", " ")
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id.length")))
                .andExpect(content().string(containsString("password.length")))
                .andExpect(content().string(containsString("name.required")))
                .andExpect(content().string(containsString("email.format")));
        assertThat(users.findByLoginId("ab")).isEmpty();
    }

    @Test
    void fr001_loggedInSignupKeepsTheCurrentSession() throws Exception {
        var session = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(post("/signup").with(csrf()).session(session)
                        .param("loginId", "other1")
                        .param("password", "Secretpass1")
                        .param("name", "다른")
                        .param("email", "other1@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("session.kept")));
        assertThat(users.findByLoginId("other1")).isEmpty();
        mockMvc.perform(get("/me").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("관리자")));
    }

    @Test
    void fr002_idBoundariesAndPasswordSameAsId() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "abcd")
                        .param("password", "Secretpass1")
                        .param("name", "사자")
                        .param("email", "four@example.com"))
                .andExpect(status().isFound());
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "a" + "b".repeat(19))
                        .param("password", "Secretpass1")
                        .param("name", "이십")
                        .param("email", "twenty@example.com"))
                .andExpect(status().isFound());
        assertThat(users.findByLoginId("abcd")).isPresent();
        assertThat(users.findByLoginId("a" + "b".repeat(19))).isPresent();

        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "abc")
                        .param("password", "Secretpass1")
                        .param("name", "셋")
                        .param("email", "three@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id.length")));
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "a" + "b".repeat(20))
                        .param("password", "Secretpass1")
                        .param("name", "이십일")
                        .param("email", "longid@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id.length")));
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "sameasid1")
                        .param("password", "sameasid1")
                        .param("name", "동일")
                        .param("email", "same@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("password.sameAsId")));
        assertThat(users.findByLoginId("abc")).isEmpty();
        assertThat(users.findByLoginId("sameasid1")).isEmpty();
    }

    @Test
    void fr003_fr026_signupAsAdminIsDuplicate() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "Admin")
                        .param("password", "Secretpass1")
                        .param("name", "다른관리자")
                        .param("email", "notadmin@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id.duplicate")));
        assertThat(users.findByLoginId("admin").orElseThrow().getName()).isEqualTo("관리자");
    }

    @Test
    void fr003_fr004_withdrawnIdAndEmailCannotBeReused() throws Exception {
        saveUser("gone2", USER_PASSWORD, "탈퇴", "gone2@example.com", com.poc.usermanagement.user.Role.USER,
                com.poc.usermanagement.user.AccountStatus.WITHDRAWN, java.time.Instant.now());
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "gone2")
                        .param("password", "Secretpass1")
                        .param("name", "재가입")
                        .param("email", "fresh2@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id.duplicate")));
        mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "fresh2")
                        .param("password", "Secretpass1")
                        .param("name", "재가입")
                        .param("email", "GONE2@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("email.duplicate")));
        assertThat(users.findByLoginId("fresh2")).isEmpty();
        assertThat(users.findByLoginId("gone2").orElseThrow().getStatus().name()).isEqualTo("WITHDRAWN");
    }
}
