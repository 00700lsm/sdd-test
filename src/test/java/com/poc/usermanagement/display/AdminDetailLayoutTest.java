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

class AdminDetailLayoutTest extends AbstractWebTest {

    @Test
    void s002Fr014_detailOrderLinkAndErrorsStayOnTheSubmittedForm() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("goneuser", USER_PASSWORD, "탈퇴자", "goneuser@example.com", Role.USER, AccountStatus.WITHDRAWN, Instant.now());
        var session = loginSession("admin", ADMIN_PASSWORD);

        String active = mockMvc.perform(get("/admin/users/member1").session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ScreenHtml.order(active, "<h1>사용자 상세</h1>", "<dl>", "아이디", "이름", "이메일", "권한", "상태", "/status\"", "/role\"", "href=\"/admin/users\"");
        assertThat(active.substring(active.indexOf("<dl>"), active.indexOf("</dl>"))).doesNotContain("탈퇴 시각");

        String statusFailed = mockMvc.perform(post("/admin/users/member1/status").with(csrf()).session(session).param("status", "WITHDRAWN"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String statusForm = ScreenHtml.form(statusFailed, "/admin/users/member1/status");
        String roleForm = ScreenHtml.form(statusFailed, "/admin/users/member1/role");
        assertThat(statusForm).contains("status.invalid");
        assertThat(statusForm.indexOf("status.invalid")).isLessThan(statusForm.indexOf("name=\"status\""));
        assertThat(roleForm).doesNotContain("status.invalid");
        assertThat(ScreenHtml.countOf(statusFailed, "status.invalid")).isEqualTo(1);

        String roleFailed = mockMvc.perform(post("/admin/users/member1/role").with(csrf()).session(session).param("role", "GUEST"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(ScreenHtml.form(roleFailed, "/admin/users/member1/role")).contains("role.invalid");
        assertThat(ScreenHtml.form(roleFailed, "/admin/users/member1/status")).doesNotContain("role.invalid");

        String withdrawn = mockMvc.perform(get("/admin/users/goneuser").session(session)).andReturn().getResponse().getContentAsString();
        String summary = withdrawn.substring(withdrawn.indexOf("<dl>"), withdrawn.indexOf("</dl>"));
        ScreenHtml.order(summary, "상태", "탈퇴 시각");
        assertThat(withdrawn).contains("action=\"/admin/users/goneuser/status\"");
        assertThat(withdrawn).contains("action=\"/admin/users/goneuser/role\"");
    }

    @Test
    void s002Fr015_statusAndRoleChoicesAreSeparate() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("admin", ADMIN_PASSWORD);
        String html = mockMvc.perform(get("/admin/users/member1").session(session)).andReturn().getResponse().getContentAsString();
        String status = ScreenHtml.selectNamed(html, "status");
        String role = ScreenHtml.selectNamed(html, "role");
        assertThat(status).contains("value=\"ACTIVE\"");
        assertThat(status).contains("value=\"INACTIVE\"");
        assertThat(status).doesNotContain("WITHDRAWN");
        assertThat(role).contains("value=\"USER\"");
        assertThat(role).contains("value=\"ADMIN\"");
        assertThat(ScreenHtml.form(html, "/admin/users/member1/status")).doesNotContain("name=\"role\"");
        assertThat(ScreenHtml.form(html, "/admin/users/member1/role")).doesNotContain("name=\"status\"");
    }
}
