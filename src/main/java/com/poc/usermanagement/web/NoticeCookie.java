package com.poc.usermanagement.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class NoticeCookie {

    static final String WITHDRAWN = "탈퇴했습니다.";

    private static final String NAME = "notice";

    private NoticeCookie() {
    }

    static void writeWithdrawn(HttpServletResponse response) {
        Cookie cookie = new Cookie(NAME, URLEncoder.encode(WITHDRAWN, StandardCharsets.UTF_8));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(120);
        response.addCookie(cookie);
    }

    static String readWithdrawn(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (!NAME.equals(cookie.getName())) {
                continue;
            }
            String value = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            if (WITHDRAWN.equals(value)) {
                return value;
            }
        }
        return null;
    }

    static void clear(HttpServletResponse response) {
        Cookie cookie = new Cookie(NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
