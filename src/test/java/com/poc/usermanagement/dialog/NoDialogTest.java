package com.poc.usermanagement.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NoDialogTest extends AbstractWebTest {

    @Test
    void s003Fr007_loginLogoutSearchAndValidationDoNotOpenDialogs() throws Exception {
        String loginFailed = mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "admin")
                        .param("password", "not-the-password"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertQuiet(loginFailed);
        assertThat(loginFailed).contains("login.failed");

        String signupFailed = mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "ab")
                        .param("password", "short")
                        .param("name", "")
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertQuiet(signupFailed);

        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        String profileFailed = mockMvc.perform(post("/me").with(csrf()).session(session)
                        .param("name", "")
                        .param("email", "bad"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(profileFailed).doesNotContain("id=\"notice-dialog\"");
        assertThat(form(profileFailed, "/me\"")).doesNotContain("data-confirm");

        String passwordFailed = mockMvc.perform(post("/me/password").with(csrf()).session(session)
                        .param("currentPassword", "not-the-password")
                        .param("newPassword", "short"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(passwordFailed).doesNotContain("id=\"notice-dialog\"");
        assertThat(form(passwordFailed, "/me/password\"")).doesNotContain("data-confirm");
        assertThat(passwordFailed).contains("password.currentMismatch");

        String me = mockMvc.perform(get("/me").session(session)).andReturn().getResponse().getContentAsString();
        assertThat(form(me, "/logout\"")).doesNotContain("data-confirm");

        String users = mockMvc.perform(get("/admin/users").session(loginSession("admin", ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(form(users, "/admin/users\"")).doesNotContain("data-confirm");
    }

    @Test
    void s003Fr008_s003Fr010_s003Fr011_rejectedCommandsKeepStoredResultsAndFailureCodes() throws Exception {
        var admin = loginSession("admin", ADMIN_PASSWORD);
        String withdrawal = mockMvc.perform(post("/me/withdrawal").with(csrf()).session(admin)
                        .param("currentPassword", ADMIN_PASSWORD))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(withdrawal).contains("admin.last");
        assertThat(withdrawal).doesNotContain("id=\"notice-dialog\"");
        assertThat(users.findByLoginId("admin").orElseThrow().getStatus()).isEqualTo(AccountStatus.ACTIVE);

        String status = mockMvc.perform(post("/admin/users/admin/status").with(csrf()).session(admin)
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(status).contains("admin.last");
        assertThat(status).doesNotContain("상태를 변경했습니다.");
        assertThat(users.findByLoginId("admin").orElseThrow().getStatus()).isEqualTo(AccountStatus.ACTIVE);

        String role = mockMvc.perform(post("/admin/users/admin/role").with(csrf()).session(admin)
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(role).contains("admin.last");
        assertThat(role).doesNotContain("권한을 변경했습니다.");
        assertThat(users.findByLoginId("admin").orElseThrow().getRole()).isEqualTo(Role.ADMIN);

        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var member = loginSession("member1", USER_PASSWORD);
        String wrongPassword = mockMvc.perform(post("/me/withdrawal").with(csrf()).session(member)
                        .param("currentPassword", "not-the-password"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(wrongPassword).contains("password.currentMismatch");
        assertThat(wrongPassword).doesNotContain("id=\"notice-dialog\"");
        int form = wrongPassword.indexOf("action=\"/me/withdrawal\"");
        int code = wrongPassword.indexOf("password.currentMismatch");
        int field = wrongPassword.indexOf("name=\"currentPassword\"", form);
        assertThat(code).isGreaterThan(form);
        assertThat(code).isLessThan(field);
        assertThat(users.findByLoginId("member1").orElseThrow().getWithdrawnAt()).isNull();
    }

    private static void assertQuiet(String html) {
        assertThat(html).doesNotContain("data-confirm");
        assertThat(html).doesNotContain("id=\"notice-dialog\"");
        assertThat(html).doesNotContain("id=\"confirm-dialog\"");
    }

    private static String form(String html, String actionMarker) {
        int start = html.indexOf("<form");
        while (start >= 0) {
            int end = html.indexOf("</form>", start);
            String form = html.substring(start, end);
            if (form.contains("action=\"" + actionMarker) || form.contains(actionMarker)) {
                return form;
            }
            start = html.indexOf("<form", end);
        }
        throw new AssertionError("form missing: " + actionMarker);
    }
}
