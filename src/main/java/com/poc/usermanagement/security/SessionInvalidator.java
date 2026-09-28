package com.poc.usermanagement.security;

import java.util.List;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
public class SessionInvalidator {

    private final SessionRegistry sessionRegistry;

    public SessionInvalidator(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void invalidate(String loginId) {
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(loginId, false);
        for (SessionInformation session : sessions) {
            session.expireNow();
        }
    }
}
