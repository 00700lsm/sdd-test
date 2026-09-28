package com.poc.usermanagement.web;

import com.poc.usermanagement.user.AdminUserCommandService;
import com.poc.usermanagement.user.AdminUserQueryService;
import com.poc.usermanagement.user.UserAccount;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AdminUserController {

    private final AdminUserQueryService queryService;
    private final AdminUserCommandService commandService;

    public AdminUserController(AdminUserQueryService queryService, AdminUserCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    /** Spec: FR-018, FR-019 */
    @GetMapping("/admin/users")
    public String list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", required = false) String page,
            @RequestParam(name = "includeWithdrawn", required = false) String includeWithdrawn,
            Model model) {
        String q = query == null ? "" : query;
        model.addAttribute("q", q);
        Boolean withdrawn = parseWithdrawn(includeWithdrawn);
        Integer pageNumber = parsePage(page);
        if (withdrawn == null || pageNumber == null) {
            model.addAttribute("errors", List.of(new FieldFailure(
                    withdrawn == null ? "includeWithdrawn" : "page",
                    withdrawn == null ? FieldErrorCodes.WITHDRAWN_INVALID : FieldErrorCodes.PAGE_INVALID)));
            model.addAttribute("errorForm", "list");
            model.addAttribute("users", List.of());
            model.addAttribute("pageNumber", 1);
            model.addAttribute("hasPrevious", false);
            model.addAttribute("hasNext", false);
            model.addAttribute("includeWithdrawn", false);
            return "admin/users";
        }
        AdminUserQueryService.UserPage result = queryService.search(q, pageNumber, withdrawn);
        model.addAttribute("errors", List.of());
        model.addAttribute("errorForm", "");
        model.addAttribute("users", result.users());
        model.addAttribute("pageNumber", result.pageNumber());
        model.addAttribute("hasPrevious", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("includeWithdrawn", withdrawn);
        return "admin/users";
    }

    /** Spec: FR-020 */
    @GetMapping("/admin/users/{loginId}")
    public String detail(@PathVariable String loginId, Model model) {
        UserAccount account = queryService.detail(loginId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("account", account);
        model.addAttribute("errors", List.of());
        model.addAttribute("errorForm", "");
        return "admin/user-detail";
    }

    /** Spec: FR-021, FR-023 */
    @PostMapping("/admin/users/{loginId}/status")
    public String status(@PathVariable String loginId, @RequestParam(name = "status", defaultValue = "") String status, Model model) {
        return commandResult(loginId, "status", commandService.changeStatus(loginId, status), model);
    }

    /** Spec: FR-022, FR-023 */
    @PostMapping("/admin/users/{loginId}/role")
    public String role(@PathVariable String loginId, @RequestParam(name = "role", defaultValue = "") String role, Model model) {
        return commandResult(loginId, "role", commandService.changeRole(loginId, role), model);
    }

    private String commandResult(String loginId, String errorForm, java.util.List<FieldFailure> errors, Model model) {
        UserAccount account = queryService.detail(loginId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("account", account);
        model.addAttribute("errors", errors);
        model.addAttribute("errorForm", errors.isEmpty() ? "" : errorForm);
        if (errors.isEmpty()) {
            model.addAttribute("notice", "status".equals(errorForm) ? "상태를 변경했습니다." : "권한을 변경했습니다.");
        }
        return "admin/user-detail";
    }

    private static Boolean parseWithdrawn(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return null;
    }

    private static Integer parsePage(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        if (!value.chars().allMatch(Character::isDigit) || value.length() > 9) {
            return null;
        }
        int page = Integer.parseInt(value);
        return page >= 1 ? page : null;
    }
}
