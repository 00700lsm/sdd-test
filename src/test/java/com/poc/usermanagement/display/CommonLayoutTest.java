package com.poc.usermanagement.display;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CommonLayoutTest extends AbstractWebTest {

    @Test
    void s002Fr001_eachScreenStartsWithItsName() throws Exception {
        assertFirstHeading(mockMvc.perform(get("/login")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "로그인");
        assertFirstHeading(mockMvc.perform(get("/signup")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "회원가입");

        var user = loginSession("admin", ADMIN_PASSWORD);
        assertFirstHeading(mockMvc.perform(get("/me").session(user)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "내 정보");
        assertFirstHeading(mockMvc.perform(get("/admin/users").session(user)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "사용자 목록");
        assertFirstHeading(mockMvc.perform(get("/admin/users/admin").session(user)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "사용자 상세");
    }

    @Test
    void s002Fr002_columnIsCenteredAndAtMost640px() throws Exception {
        var resource = getClass().getClassLoader().getResource("static/layout.css");
        assertThat(resource).isNotNull();
        String css = Files.readString(Path.of(resource.toURI()));
        assertThat(css).contains("max-width: 640px");
        assertThat(css).contains("margin-inline: auto");
        assertThat(css).contains("overflow-x: auto");
        mockMvc.perform(get("/login"))
                .andExpect(content().string(containsString("layout.css")))
                .andExpect(content().string(containsString("class=\"column\"")));
        mockMvc.perform(get("/layout.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("max-width: 640px")));
    }

    private static void assertFirstHeading(String html, String title) {
        int main = html.indexOf("<main");
        int heading = html.indexOf("<h1>");
        assertThat(main).isGreaterThanOrEqualTo(0);
        assertThat(heading).isGreaterThan(main);
        assertThat(html.substring(main, heading).replaceAll("<main[^>]*>", "").trim()).isEmpty();
        assertThat(html).contains("<h1>" + title + "</h1>");
    }
}
