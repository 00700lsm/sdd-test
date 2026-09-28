package com.poc.usermanagement.display;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminListLayoutTest extends AbstractWebTest {

    @Test
    void s002Fr011_listOrderPagingAndSearchErrors() throws Exception {
        Instant start = users.findByLoginId("admin").orElseThrow().getCreatedAt().plusSeconds(1);
        for (int i = 1; i <= 20; i++) {
            String id = "p" + String.format("%02d", i);
            saveUser(id, USER_PASSWORD, "이름" + i, id + "@example.com", Role.USER, AccountStatus.ACTIVE, start.plusSeconds(i));
        }
        var session = loginSession("admin", ADMIN_PASSWORD);
        String first = mockMvc.perform(get("/admin/users").session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ScreenHtml.order(first, "<h1>사용자 목록</h1>", ">검색어<", ">탈퇴 포함<", "id=\"page-number\"", "<table", "href=\"/me\">내 정보");
        assertThat(first).contains(">다음<");
        assertThat(first).doesNotContain(">이전<");
        String search = ScreenHtml.form(first, "/admin/users");
        ScreenHtml.order(search, "name=\"q\"", "name=\"includeWithdrawn\"", ">검색<");

        String second = mockMvc.perform(get("/admin/users").session(session).param("page", "2")).andReturn().getResponse().getContentAsString();
        assertThat(second).contains(">이전<");
        assertThat(second).doesNotContain(">다음<");

        String invalidPage = mockMvc.perform(get("/admin/users").session(session).param("page", "0")).andReturn().getResponse().getContentAsString();
        String invalidForm = ScreenHtml.form(invalidPage, "/admin/users");
        assertThat(invalidForm).contains("page.invalid");
        assertThat(invalidForm.indexOf("page.invalid")).isLessThan(invalidForm.indexOf("name=\"q\""));
        assertThat(ScreenHtml.countOf(invalidPage, "page.invalid")).isEqualTo(1);

        String invalidWithdrawn = mockMvc.perform(get("/admin/users").session(session).param("includeWithdrawn", "yes"))
                .andReturn().getResponse().getContentAsString();
        assertThat(ScreenHtml.form(invalidWithdrawn, "/admin/users")).contains("withdrawn.invalid");
        assertThat(ScreenHtml.countOf(invalidWithdrawn, "withdrawn.invalid")).isEqualTo(1);
    }

    @Test
    void s002Fr012_columnsWithdrawalCellAndDetailLink() throws Exception {
        String name = "가".repeat(50);
        String email = "a".repeat(249) + "@b.co";
        saveUser("longuser", USER_PASSWORD, name, email, Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("goneuser", USER_PASSWORD, "탈퇴자", "goneuser@example.com", Role.USER, AccountStatus.WITHDRAWN, Instant.now());
        var session = loginSession("admin", ADMIN_PASSWORD);
        String html = mockMvc.perform(get("/admin/users").session(session).param("includeWithdrawn", "true"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ScreenHtml.order(html, "<th>아이디</th>", "<th>이름</th>", "<th>이메일</th>", "<th>권한</th>", "<th>상태</th>", "<th>탈퇴 시각</th>");
        String active = ScreenHtml.rowContaining(html, "longuser");
        List<String> cells = ScreenHtml.cells(active);
        assertThat(cells).hasSize(6);
        assertThat(cells.get(0)).isEqualTo("longuser");
        assertThat(cells.get(1)).isEqualTo(name);
        assertThat(cells.get(2)).isEqualTo(email);
        assertThat(cells.get(5)).isEmpty();
        assertThat(ScreenHtml.countOf(active, "<a ")).isEqualTo(1);
        assertThat(active).contains("href=\"/admin/users/longuser\"");

        String withdrawn = ScreenHtml.rowContaining(html, "goneuser");
        assertThat(ScreenHtml.cells(withdrawn).get(5)).isNotBlank();
    }

    @Test
    void s002Fr013_emptyResultHidesUserRows() throws Exception {
        var session = loginSession("admin", ADMIN_PASSWORD);
        String html = mockMvc.perform(get("/admin/users").session(session).param("q", "없는사람"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(html).contains("결과 없음");
        assertThat(html).contains("<th>아이디</th>");
        assertThat(html).doesNotContain("<td");
        assertThat(html).doesNotContain(">이전<");
        assertThat(html).doesNotContain(">다음<");
    }
}
