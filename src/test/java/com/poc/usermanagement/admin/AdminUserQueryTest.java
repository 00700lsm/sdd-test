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

class AdminUserQueryTest extends AbstractWebTest {

    @Test
    void fr018_fr019_fr026_seededAdminSeesPagedUsersAndSearch() throws Exception {
        Instant start = users.findByLoginId("admin").orElseThrow().getCreatedAt().plusSeconds(1);
        for (int i = 1; i <= 20; i++) {
            String id = "p" + String.format("%02d", i);
            saveUser(id, USER_PASSWORD, "이름" + i, id + "@example.com", Role.USER, AccountStatus.ACTIVE, start.plusSeconds(i));
        }
        saveUser("hidden1", USER_PASSWORD, "탈퇴자", "hidden1@example.com", Role.USER, AccountStatus.WITHDRAWN, start.plusSeconds(100));
        var session = loginSession("admin", ADMIN_PASSWORD);

        mockMvc.perform(get("/admin/users").session(session).param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("p20")))
                .andExpect(content().string(not(containsString(">admin<"))))
                .andExpect(content().string(not(containsString("hidden1"))));
        mockMvc.perform(get("/admin/users").session(session).param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">admin<")))
                .andExpect(content().string(not(containsString(">p20<"))));
        mockMvc.perform(get("/admin/users").session(session).param("page", "1"))
                .andExpect(content().string(containsString(">p20<")));

        mockMvc.perform(get("/admin/users").session(session).param("q", "이름20"))
                .andExpect(content().string(containsString("p20")))
                .andExpect(content().string(not(containsString(">p01<"))));
        mockMvc.perform(get("/admin/users").session(session).param("q", "P20@EXAMPLE.COM"))
                .andExpect(content().string(containsString("p20")));
        mockMvc.perform(get("/admin/users").session(session).param("q", ""))
                .andExpect(content().string(containsString("p20")));
        mockMvc.perform(get("/admin/users").session(session).param("q", "없는검색어"))
                .andExpect(content().string(containsString("결과 없음")));
        mockMvc.perform(get("/admin/users").session(session).param("includeWithdrawn", "true").param("q", "hidden1"))
                .andExpect(content().string(containsString("hidden1")));
    }

    @Test
    void fr019_searchTreatsPercentAndUnderscoreAsLiterals() throws Exception {
        saveUser("pctuser", USER_PASSWORD, "100%done", "pctuser@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("wild1", USER_PASSWORD, "100Xdone", "wild1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("under1", USER_PASSWORD, "a_b", "under1@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        saveUser("wild2", USER_PASSWORD, "axb", "wild2@example.com", Role.USER, AccountStatus.ACTIVE, Instant.now());
        var session = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(get("/admin/users").session(session).param("q", "100%"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("pctuser")))
                .andExpect(content().string(not(containsString("wild1"))));
        mockMvc.perform(get("/admin/users").session(session).param("q", "a_b"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("under1")))
                .andExpect(content().string(not(containsString("wild2"))));
    }

    @Test
    void fr018_pagePastTheEndStaysOnTheLastPage() throws Exception {
        var session = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(get("/admin/users").session(session).param("page", "9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"page-number\">1<")));
    }

    @Test
    void fr018_defaultListIncludesInactiveAndExcludesWithdrawn() throws Exception {
        saveUser("paused1", USER_PASSWORD, "중지", "paused1@example.com", Role.USER, AccountStatus.INACTIVE, Instant.now());
        saveUser("oldgone", USER_PASSWORD, "탈퇴", "oldgone@example.com", Role.USER, AccountStatus.WITHDRAWN, Instant.now());
        var session = loginSession("admin", ADMIN_PASSWORD);
        mockMvc.perform(get("/admin/users").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("paused1")))
                .andExpect(content().string(containsString("INACTIVE")))
                .andExpect(content().string(not(containsString("oldgone"))));
    }
}
