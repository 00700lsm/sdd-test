package com.poc.usermanagement.display;

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

class MeLayoutTest extends AbstractWebTest {

    @Test
    void s002Fr008_profileOrderAndPasswordErrorsStayOnThatForm() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        String page = mockMvc.perform(get("/me").session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ScreenHtml.order(page, "<h1>내 정보</h1>", "<dl>", "아이디", "이름", "이메일", "권한", "상태", "action=\"/me\"", "action=\"/me/password\"", "action=\"/me/withdrawal\"", "action=\"/logout\"");
        String summary = page.substring(page.indexOf("<dl>"), page.indexOf("</dl>"));
        assertThat(summary).doesNotContain("<input");

        String failed = mockMvc.perform(post("/me/password").with(csrf()).session(session)
                        .param("currentPassword", "not-the-password")
                        .param("newPassword", "short"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String passwordForm = ScreenHtml.form(failed, "/me/password");
        assertThat(passwordForm).contains("password.currentMismatch");
        assertThat(passwordForm).contains("password.length");
        assertThat(passwordForm.indexOf("password.currentMismatch")).isLessThan(passwordForm.indexOf("name=\"currentPassword\""));
        assertThat(ScreenHtml.form(failed, "/me")).doesNotContain("password.currentMismatch");
        assertThat(ScreenHtml.form(failed, "/me/withdrawal")).doesNotContain("password.currentMismatch");
        int heading = failed.indexOf("<h1>내 정보</h1>");
        int summaryStart = failed.indexOf("<dl>");
        assertThat(failed.substring(heading, summaryStart)).doesNotContain("password.currentMismatch");
        assertThat(ScreenHtml.countOf(failed, "password.currentMismatch")).isEqualTo(1);
    }

    @Test
    void s002Fr009_profileFormDoesNotContainOtherActions() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        String html = mockMvc.perform(get("/me").session(session)).andReturn().getResponse().getContentAsString();
        String profile = ScreenHtml.form(html, "/me");
        assertThat(profile).doesNotContain("회원탈퇴");
        assertThat(profile).doesNotContain("로그아웃");
        ScreenHtml.order(html, "action=\"/me/password\"", "action=\"/me/withdrawal\"", "action=\"/logout\"");
    }

    @Test
    void s002Fr010_adminLinkOnlyBelowLogout() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        String userPage = mockMvc.perform(get("/me").session(loginSession("member1", USER_PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        assertThat(userPage).doesNotContain("사용자 목록");

        String adminPage = mockMvc.perform(get("/me").session(loginSession("admin", ADMIN_PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        int logout = adminPage.indexOf(">로그아웃<");
        int link = adminPage.indexOf("사용자 목록");
        assertThat(link).isGreaterThan(logout);
        assertThat(adminPage).contains("href=\"/admin/users\"");
    }
}
