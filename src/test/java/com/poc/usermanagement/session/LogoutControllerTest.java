package com.poc.usermanagement.session;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import org.junit.jupiter.api.Test;

class LogoutControllerTest extends AbstractWebTest {

    @Test
    void fr009_fr017_logoutEndsTheSessionAndAnonymousLogoutIsRejected() throws Exception {
        var session = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/me").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("auth.required")));
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("auth.required")));
    }
}
