package com.poc.usermanagement.web;

import com.poc.usermanagement.security.AccountReloadFilter;
import com.poc.usermanagement.user.LoginService;
import com.poc.usermanagement.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SessionController {

    private final LoginService loginService;
    private final SessionRegistry sessionRegistry;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public SessionController(LoginService loginService, SessionRegistry sessionRegistry) {
        this.loginService = loginService;
        this.sessionRegistry = sessionRegistry;
    }

    /** Spec: FR-007 */
    @GetMapping("/login")
    public String form(HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("errors", List.of());
        model.addAttribute("loginId", "");
        String withdrawnNotice = NoticeCookie.readWithdrawn(request);
        if (withdrawnNotice != null) {
            model.addAttribute("notice", withdrawnNotice);
            NoticeCookie.clear(response);
        }
        return "login";
    }

    /** Spec: FR-007, FR-008, FR-014, FR-026 */
    @PostMapping("/login")
    public String login(
            @RequestParam(name = "loginId", defaultValue = "") String loginId,
            @RequestParam(name = "password", defaultValue = "") String password,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        if (authenticated()) {
            model.addAttribute("errors", List.of(new FieldFailure("session", FieldErrorCodes.SESSION_KEPT)));
            model.addAttribute("loginId", loginId);
            return "login";
        }
        Optional<UserAccount> account = loginService.authenticate(loginId, password);
        if (account.isEmpty()) {
            model.addAttribute("errors", List.of(new FieldFailure("login", FieldErrorCodes.LOGIN_FAILED)));
            model.addAttribute("loginId", loginId);
            return "login";
        }
        UserAccount user = account.get();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getLoginId(),
                null,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        AccountReloadFilter.authority(user.getRole()))));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            request.changeSessionId();
        }
        HttpSession session = request.getSession(true);
        sessionRegistry.registerNewSession(session.getId(), user.getLoginId());
        securityContextRepository.saveContext(context, request, response);
        return "redirect:/me";
    }

    /** Spec: FR-009, FR-017 */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            sessionRegistry.removeSessionInformation(session.getId());
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    private static boolean authenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
