package com.poc.usermanagement.user;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserQueryService {

    public static final int PAGE_SIZE = 20;

    private final UserAccountRepository users;

    public AdminUserQueryService(UserAccountRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UserPage search(String query, int pageNumber, boolean includeWithdrawn) {
        String pattern = AccountRules.likePattern(query);
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("loginId"));
        Page<UserAccount> result = users.search(includeWithdrawn, pattern, PageRequest.of(0, PAGE_SIZE, sort));
        int totalPages = result.getTotalPages();
        int resolved = pageNumber;
        if (totalPages == 0) {
            resolved = 1;
        } else if (pageNumber > totalPages) {
            resolved = totalPages;
        }
        if (resolved != 1 || pageNumber != 1) {
            int index = Math.max(resolved, 1) - 1;
            result = users.search(includeWithdrawn, pattern, PageRequest.of(index, PAGE_SIZE, sort));
        }
        return new UserPage(result.getContent(), resolved, resolved > 1, totalPages > resolved);
    }

    @Transactional(readOnly = true)
    public UserAccount detail(String loginId) {
        return users.findByLoginId(AccountRules.normalizeId(loginId)).orElse(null);
    }

    public record UserPage(List<UserAccount> users, int pageNumber, boolean hasPrevious, boolean hasNext) {
    }
}
