package com.example.webstarter.admin;

import java.time.Clock;
import java.util.List;

import com.example.webstarter.user.AppUser;
import com.example.webstarter.user.AppUserRepository;
import com.example.webstarter.user.Role;
import com.example.webstarter.user.UserIdentityNormalizer;
import com.example.webstarter.user.UserOperationException;
import com.example.webstarter.user.UserSummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserService {

    private final AppUserRepository userRepository;
    private final Clock clock;

    public AdminUserService(AppUserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<UserSummary> list(int page, int pageSize) {
        int safePage = Math.max(page, 0);
        return userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(safePage, pageSize))
                .map(user -> UserSummary.from(user, clock.instant()));
    }

    @Transactional
    public UserSummary toggleEnabled(String actorUsername, Long userId) {
        List<AppUser> administrators = userRepository.findAllByRoleForUpdate(Role.ADMIN);
        AppUser target = findFromLockedAdministratorsOrById(administrators, userId);
        requireNotSelf(actorUsername, target, "본인 계정은 비활성화할 수 없습니다.");
        if (target.isEnabled()
                && target.hasRole(Role.ADMIN)
                && administrators.stream().filter(AppUser::isEnabled).count() <= 1) {
            throw new UserOperationException("마지막 활성 관리자 계정은 비활성화할 수 없습니다.");
        }
        target.setEnabled(!target.isEnabled(), clock.instant());
        return UserSummary.from(target, clock.instant());
    }

    @Transactional
    public UserSummary unlock(Long userId) {
        AppUser target = findForUpdate(userId);
        target.unlock(clock.instant());
        return UserSummary.from(target, clock.instant());
    }

    @Transactional
    public UserSummary changeRole(String actorUsername, Long userId, Role role) {
        List<AppUser> administrators = userRepository.findAllByRoleForUpdate(Role.ADMIN);
        AppUser target = findFromLockedAdministratorsOrById(administrators, userId);
        if (role == Role.USER && target.hasRole(Role.ADMIN)) {
            requireNotSelf(actorUsername, target, "본인의 관리자 권한은 제거할 수 없습니다.");
            if (administrators.size() <= 1) {
                throw new UserOperationException("마지막 관리자 권한은 제거할 수 없습니다.");
            }
        }
        target.setPrimaryRole(role, clock.instant());
        return UserSummary.from(target, clock.instant());
    }

    private AppUser findForUpdate(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserOperationException("대상 사용자를 찾을 수 없습니다."));
    }

    private AppUser findFromLockedAdministratorsOrById(List<AppUser> administrators, Long userId) {
        return administrators.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseGet(() -> findForUpdate(userId));
    }

    private void requireNotSelf(String actorUsername, AppUser target, String message) {
        if (target.getUsername().equals(UserIdentityNormalizer.username(actorUsername))) {
            throw new UserOperationException(message);
        }
    }
}
