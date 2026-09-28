package com.poc.usermanagement.admin;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.usermanagement.support.AbstractWebTest;
import com.poc.usermanagement.user.AccountStatus;
import com.poc.usermanagement.user.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AdminUserDetailTest extends AbstractWebTest {

    @Test
    void fr016_fr020_adminCanSeeWithdrawnDetailAndUserCannot() throws Exception {
        saveUser("gone1", USER_PASSWORD, "탈퇴자", "gone1@example.com", Role.USER, AccountStatus.WITHDRAWN, Instant.now());
        var admin = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(get("/admin/users/gone1").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("gone1")))
                .andExpect(content().string(containsString("WITHDRAWN")))
                .andExpect(content().string(containsString("2026-01-01T00:00:00Z")))
                .andExpect(content().string(not(containsString("$2"))));

        saveUser("member1", USER_PASSWORD, "회원", "member1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var user = loginSession("member1", USER_PASSWORD);
        mockMvc.perform(get("/admin/users/gone1").session(user))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("auth.forbidden")))
                .andExpect(content().string(not(containsString("gone1@example.com"))));
    }
}
