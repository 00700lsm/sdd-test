package com.poc.usermanagement.web;

import com.poc.usermanagement.user.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SignupController {

    private final RegistrationService registrationService;

    public SignupController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /** Spec: FR-001 */
    @GetMapping("/signup")
    public String form(Model model) {
        model.addAttribute("signup", new SignupForm());
        model.addAttribute("errors", List.of());
        return "signup";
    }

    /** Spec: FR-001, FR-002, FR-003, FR-004, FR-005, FR-006 */
    @PostMapping("/signup")
    public String submit(
            @ModelAttribute("signup") SignupForm signup,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (authenticated()) {
            model.addAttribute("errors", List.of(new FieldFailure("session", FieldErrorCodes.SESSION_KEPT)));
            return "signup";
        }
        signup.setPassword(null);
        List<FieldFailure> errors = registrationService.register(
                signup.getLoginId(), request.getParameter("password"), signup.getName(), signup.getEmail());
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "signup";
        }
        redirectAttributes.addFlashAttribute("notice", "가입되었습니다.");
        return "redirect:/login";
    }

    private static boolean authenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public static class SignupForm {
        private String loginId;
        private String name;
        private String email;

        public String getLoginId() {
            return loginId;
        }

        public void setLoginId(String loginId) {
            this.loginId = loginId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setPassword(String password) {
            // Password is read from the request and is not kept on the form.
        }
    }
}
