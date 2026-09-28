package com.poc.usermanagement.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ConfirmDialogTest extends AbstractWebTest {

    private static final String WITHDRAWAL_QUESTION = "탈퇴하면 이 계정으로 다시 로그인할 수 없습니다. 탈퇴할까요?";
    private static final String STATUS_QUESTION = "이 사용자의 상태를 변경할까요?";
    private static final String ROLE_QUESTION = "이 사용자의 권한을 변경할까요?";

    @Test
    void s003Fr001_confirmDialogHasOkAndCancelOnly() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        String page = mockMvc.perform(get("/me").session(loginSession("member1", USER_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String dialog = dialog(page, "confirm-dialog");
        assertThat(dialog).contains("type=\"button\" id=\"confirm-ok\">확인</button>");
        assertThat(dialog).contains("type=\"button\" id=\"confirm-cancel\">취소</button>");
        assertThat(dialog).doesNotContain("type=\"submit\"");
    }

    @Test
    void s003Fr002_withdrawalConfirmIsOnTheFormBeforeFailureCodes() throws Exception {
        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("member1", USER_PASSWORD);
        String failed = mockMvc.perform(post("/me/withdrawal").with(csrf()).session(session).param("currentPassword", ""))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(failed).contains("data-confirm=\"" + WITHDRAWAL_QUESTION + "\"");
        assertThat(failed).contains("action=\"/me/withdrawal\"");
        assertThat(failed).contains("novalidate");
        assertThat(failed.indexOf(WITHDRAWAL_QUESTION)).isLessThan(failed.indexOf("password.currentMismatch"));
        assertThat(failed).doesNotContain("id=\"notice-dialog\"");
        assertThat(users.findByLoginId("member1").orElseThrow().getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void s003Fr003_statusFormAsksBeforeSave() throws Exception {
        String page = mockMvc.perform(get("/admin/users/admin").session(loginSession("admin", ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(page).contains("data-confirm=\"" + STATUS_QUESTION + "\"");
        assertThat(page).contains("novalidate");
        assertThat(page).contains("action=\"/admin/users/admin/status\"");
    }

    @Test
    void s003Fr004_roleFormAsksBeforeSave() throws Exception {
        String page = mockMvc.perform(get("/admin/users/admin").session(loginSession("admin", ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(page).contains("data-confirm=\"" + ROLE_QUESTION + "\"");
        assertThat(page).contains("action=\"/admin/users/admin/role\"");
    }

    @Test
    void s003Fr008_s003Fr009_scriptStopsSubmitUntilConfirmAndCancelOnlyCloses() throws Exception {
        String script = mockMvc.perform(get("/dialogs.js"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(script).contains("form[data-confirm]");
        assertThat(script).contains("event.preventDefault()");
        assertThat(script).contains("confirmDialog.showModal()");
        assertThat(script.indexOf("event.preventDefault()")).isLessThan(script.indexOf("form.requestSubmit()"));
        int cancel = script.indexOf("confirm-cancel");
        int cancelClose = script.indexOf("confirmDialog.close()", cancel);
        int nextSubmit = script.indexOf("requestSubmit()", cancel);
        assertThat(cancelClose).isGreaterThan(cancel);
        assertThat(nextSubmit).isEqualTo(-1);
        assertThat(script).doesNotContain("password");
    }

    private static String dialog(String html, String id) {
        int start = html.indexOf("id=\"" + id + "\"");
        assertThat(start).isGreaterThanOrEqualTo(0);
        int open = html.lastIndexOf("<dialog", start);
        int close = html.indexOf("</dialog>", start);
        return html.substring(open, close);
    }
}
