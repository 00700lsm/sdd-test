package com.poc.usermanagement.display;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import org.junit.jupiter.api.Test;

class LoginSignupLayoutTest extends AbstractWebTest {

    @Test
    void s002Fr005_loginOrderAndSignupLink() throws Exception {
        String html = mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "admin")
                        .param("password", "not-the-password"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ScreenHtml.order(html, "<h1>로그인</h1>", "login.failed", "name=\"loginId\"", "name=\"password\"", ">로그인<", "href=\"/signup\"");
        String form = ScreenHtml.form(html, "/login");
        assertThat(form).contains("login.failed");
        assertThat(form).doesNotContain("name=\"email\"");
        assertThat(form).doesNotContain("name=\"name\"");
        assertThat(ScreenHtml.inputTag(form, "loginId")).contains("value=\"admin\"");
        assertThat(ScreenHtml.inputTag(form, "password")).doesNotContain("value=");
        assertThat(html).doesNotContain("not-the-password");
    }

    @Test
    void s002Fr006_signupOrderAndLoginLink() throws Exception {
        String html = mockMvc.perform(get("/signup")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ScreenHtml.order(html, "<h1>회원가입</h1>", "name=\"loginId\"", "name=\"password\"", "name=\"name\"", "name=\"email\"", ">가입<", "href=\"/login\"");
    }

    @Test
    void s002Fr007_failedSignupKeepsTextAndClearsPassword() throws Exception {
        String html = mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "valid1")
                        .param("password", "Secretpass1")
                        .param("name", "홍길동")
                        .param("email", "bad"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String form = ScreenHtml.form(html, "/signup");
        assertThat(ScreenHtml.inputTag(form, "loginId")).contains("value=\"valid1\"");
        assertThat(ScreenHtml.inputTag(form, "name")).contains("value=\"홍길동\"");
        assertThat(ScreenHtml.inputTag(form, "email")).contains("value=\"bad\"");
        assertThat(ScreenHtml.inputTag(form, "password")).doesNotContain("value=");
        assertThat(html).doesNotContain("Secretpass1");
    }
}
