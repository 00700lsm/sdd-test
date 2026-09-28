package com.poc.usermanagement.user;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends Repository<UserAccount, Long> {

    UserAccount save(UserAccount user);

    Optional<UserAccount> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndLoginIdNot(String email, String loginId);

    long countByRoleAndStatus(Role role, AccountStatus status);

    @Query(
            value = """
                    select u from UserAccount u
                    where (:includeWithdrawn = true or u.status <> com.poc.usermanagement.user.AccountStatus.WITHDRAWN)
                      and (
                        :pattern = ''
                        or lower(u.loginId) like :pattern escape '\\'
                        or lower(u.name) like :pattern escape '\\'
                        or lower(u.email) like :pattern escape '\\'
                      )
                    """,
            countQuery = """
                    select count(u) from UserAccount u
                    where (:includeWithdrawn = true or u.status <> com.poc.usermanagement.user.AccountStatus.WITHDRAWN)
                      and (
                        :pattern = ''
                        or lower(u.loginId) like :pattern escape '\\'
                        or lower(u.name) like :pattern escape '\\'
                        or lower(u.email) like :pattern escape '\\'
                      )
                    """)
    Page<UserAccount> search(
            @Param("includeWithdrawn") boolean includeWithdrawn,
            @Param("pattern") String pattern,
            Pageable pageable);
}
