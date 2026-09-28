package com.poc.usermanagement.web;

import com.poc.usermanagement.user.PasswordService;
import com.poc.usermanagement.user.ProfileService;
import com.poc.usermanagement.user.UserAccount;
import com.poc.usermanagement.user.WithdrawalService;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final PasswordService passwordService;
    private final WithdrawalService withdrawalService;

    public ProfileController(
            ProfileService profileService,
            PasswordService passwordService,
            WithdrawalService withdrawalService) {
        this.profileService = profileService;
        this.passwordService = passwordService;
        this.withdrawalService = withdrawalService;
    }

    /** Spec: FR-010, FR-025 */
    @GetMapping("/me")
    public String view(Authentication authentication, Model model) {
        UserAccount account = profileService.require(authentication.getName());
        model.addAttribute("account", account);
        model.addAttribute("errors", List.of());
        model.addAttribute("errorForm", "");
        return "me";
    }

    /** Spec: FR-004, FR-011, FR-025 */
    @PostMapping("/me")
    public String update(
            Authentication authentication,
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "email", defaultValue = "") String email,
            Model model,
            RedirectAttributes redirectAttributes) {
        List<FieldFailure> errors = profileService.update(authentication.getName(), name, email);
        UserAccount account = profileService.require(authentication.getName());
        model.addAttribute("account", account);
        model.addAttribute("errors", errors);
        model.addAttribute("errorForm", errors.isEmpty() ? "" : "profile");
        if (!errors.isEmpty()) {
            return "me";
        }
        redirectAttributes.addFlashAttribute("notice", "회원정보를 수정했습니다.");
        return "redirect:/me";
    }

    /** Spec: FR-002, FR-006, FR-012, FR-025 */
    @PostMapping("/me/password")
    public String password(
            Authentication authentication,
            @RequestParam(name = "currentPassword", defaultValue = "") String currentPassword,
            @RequestParam(name = "newPassword", defaultValue = "") String newPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        List<FieldFailure> errors = passwordService.change(authentication.getName(), currentPassword, newPassword);
        model.addAttribute("account", profileService.require(authentication.getName()));
        model.addAttribute("errors", errors);
        model.addAttribute("errorForm", errors.isEmpty() ? "" : "password");
        if (!errors.isEmpty()) {
            return "me";
        }
        redirectAttributes.addFlashAttribute("notice", "비밀번호를 변경했습니다.");
        return "redirect:/me";
    }

    /** Spec: FR-013, FR-023, FR-024, FR-025 */
    @PostMapping("/me/withdrawal")
    public String withdraw(
            Authentication authentication,
            @RequestParam(name = "currentPassword", defaultValue = "") String currentPassword,
            Model model,
            HttpServletResponse response) {
        String loginId = authentication.getName();
        List<FieldFailure> errors = withdrawalService.withdraw(loginId, currentPassword);
        if (!errors.isEmpty()) {
            model.addAttribute("account", profileService.require(loginId));
            model.addAttribute("errors", errors);
            model.addAttribute("errorForm", "withdrawal");
            return "me";
        }
        NoticeCookie.writeWithdrawn(response);
        return "redirect:/login";
    }
}
