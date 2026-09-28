package com.poc.usermanagement.display;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import org.junit.jupiter.api.Test;

class FieldAndErrorLayoutTest extends AbstractWebTest {

    @Test
    void s002Fr003_eachControlIsPairedWithItsNameAndButtonFollows() throws Exception {
        String login = mockMvc.perform(get("/login")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String signup = mockMvc.perform(get("/signup")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var session = loginSession("admin", ADMIN_PASSWORD);
        String me = mockMvc.perform(get("/me").session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String users = mockMvc.perform(get("/admin/users").session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String detail = mockMvc.perform(get("/admin/users/admin").session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        for (String html : new String[] {login, signup, me, users, detail}) {
            ScreenHtml.eachFieldHasOneControl(html);
            assertThat(html).doesNotContain("type=\"text\" name=\"password\"");
        }
        ScreenHtml.buttonAfterControls(ScreenHtml.form(login, "/login"));
        ScreenHtml.buttonAfterControls(ScreenHtml.form(signup, "/signup"));
        assertThat(ScreenHtml.inputTag(login, "password")).contains("type=\"password\"");
        assertThat(ScreenHtml.inputTag(signup, "password")).contains("type=\"password\"");
        assertThat(ScreenHtml.inputTag(me, "currentPassword")).contains("type=\"password\"");
    }

    @Test
    void s002Fr004_twoFailureCodesStayAboveTheInputs() throws Exception {
        String html = mockMvc.perform(post("/signup").with(csrf())
                        .param("loginId", "valid1")
                        .param("password", "short")
                        .param("name", "홍길동")
                        .param("email", "bad"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String form = ScreenHtml.form(html, "/signup");
        assertThat(form).contains("password.length");
        assertThat(form).contains("email.format");
        assertThat(ScreenHtml.countOf(html, "password.length")).isEqualTo(1);
        assertThat(ScreenHtml.countOf(html, "email.format")).isEqualTo(1);
        assertThat(form.indexOf("password.length")).isLessThan(form.indexOf("name=\"password\""));
        assertThat(form.indexOf("email.format")).isLessThan(form.indexOf("name=\"email\""));
        assertThat(ScreenHtml.countOf(form, "<p class=\"error\"")).isGreaterThanOrEqualTo(2);
    }
}
