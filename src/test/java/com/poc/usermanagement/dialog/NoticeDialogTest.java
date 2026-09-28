package com.poc.usermanagement.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

class NoticeDialogTest extends AbstractWebTest {

    @Test
    void s003Fr005_s003Fr006_s003Fr009_successNoticesUseFixedSentencesWithoutPasswords() throws Exception {
        String signupPassword = "Distinct99";
        MockHttpSession visitor = new MockHttpSession();
        mockMvc.perform(post("/signup").with(csrf()).session(visitor)
                        .param("loginId", "newuser1")
                        .param("password", signupPassword)
                        .param("name", "새회원")
                        .param("email", "newuser1@example.com"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
        String login = mockMvc.perform(get("/login").session(visitor))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertOnce(login, "가입되었습니다.");
        assertThat(dialog(login, "notice-dialog")).contains("type=\"button\" id=\"notice-ok\">확인</button>");
        assertThat(login).doesNotContain(signupPassword);
        mockMvc.perform(get("/me").session(visitor)).andExpect(status().isUnauthorized());

        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var member = loginSession("member1", USER_PASSWORD);
        mockMvc.perform(post("/me").with(csrf()).session(member)
                        .param("name", "새이름")
                        .param("email", "member1b@example.com"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/me"));
        String profile = mockMvc.perform(get("/me").session(member)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertOnce(profile, "회원정보를 수정했습니다.");
        assertThat(profile).contains("새이름");
        assertThat(profile).doesNotContain(USER_PASSWORD);

        String nextPassword = "Newpass12";
        mockMvc.perform(post("/me/password").with(csrf()).session(member)
                        .param("currentPassword", USER_PASSWORD)
                        .param("newPassword", nextPassword))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/me"));
        String passwordPage = mockMvc.perform(get("/me").session(member)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertOnce(passwordPage, "비밀번호를 변경했습니다.");
        assertThat(passwordPage).doesNotContain(nextPassword);
        assertThat(passwordPage).contains("id=\"login-id\"");

        MvcResult withdrawn = mockMvc.perform(post("/me/withdrawal").with(csrf()).session(member)
                        .param("currentPassword", nextPassword))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andReturn();
        Cookie notice = noticeCookie(withdrawn);
        String afterWithdrawal = mockMvc.perform(get("/login").cookie(notice))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertOnce(afterWithdrawal, "탈퇴했습니다.");
        assertThat(afterWithdrawal).doesNotContain(nextPassword);
        mockMvc.perform(get("/me").session(member)).andExpect(status().isUnauthorized());

        saveUser("member2", USER_PASSWORD, "회원2", "member2@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var member2 = loginSession("member2", USER_PASSWORD);
        var admin = loginSession("admin", ADMIN_PASSWORD);
        String statusPage = mockMvc.perform(post("/admin/users/member2/status").with(csrf()).session(admin)
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertOnce(statusPage, "상태를 변경했습니다.");
        String memberPage = mockMvc.perform(get("/me").session(member2)).andReturn().getResponse().getContentAsString();
        assertThat(memberPage).doesNotContain("상태를 변경했습니다.");

        saveUser("member3", USER_PASSWORD, "회원3", "member3@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var member3 = loginSession("member3", USER_PASSWORD);
        String rolePage = mockMvc.perform(post("/admin/users/member3/role").with(csrf()).session(admin)
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertOnce(rolePage, "권한을 변경했습니다.");
        mockMvc.perform(get("/me").session(member3)).andExpect(status().isOk());
    }

    private static void assertOnce(String html, String sentence) {
        assertThat(html).contains(sentence);
        assertThat(html.indexOf(sentence)).isEqualTo(html.lastIndexOf(sentence));
        assertThat(dialog(html, "notice-dialog")).contains(sentence);
    }

    private static String dialog(String html, String id) {
        int start = html.indexOf("id=\"" + id + "\"");
        assertThat(start).isGreaterThanOrEqualTo(0);
        int open = html.lastIndexOf("<dialog", start);
        int close = html.indexOf("</dialog>", start);
        return html.substring(open, close);
    }

    private static Cookie noticeCookie(MvcResult result) {
        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).isNotNull();
        for (Cookie cookie : cookies) {
            if ("notice".equals(cookie.getName())) {
                return cookie;
            }
        }
        throw new AssertionError("notice cookie missing");
    }
}
