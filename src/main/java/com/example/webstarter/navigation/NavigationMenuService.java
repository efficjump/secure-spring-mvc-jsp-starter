package com.example.webstarter.navigation;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.webstarter.config.WorkspaceProperties;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NavigationMenuService {

    private final NavigationMenuRepository repository;
    private final Clock clock;
    private final WorkspaceProperties workspaceProperties;

    public NavigationMenuService(
            NavigationMenuRepository repository,
            Clock clock,
            WorkspaceProperties workspaceProperties) {
        this.repository = repository;
        this.clock = clock;
        this.workspaceProperties = workspaceProperties;
    }

    @Transactional(readOnly = true)
    public List<NavigationMenuSummary> visibleMenus(Authentication authentication) {
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toUnmodifiableSet());
        return repository.findAllByEnabledTrueOrderByDisplayOrderAscIdAsc().stream()
                .filter(menu -> authorities.contains(menu.getRequiredRole().authority()))
                .map(NavigationMenuSummary::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<NavigationMenuSummary> listAll() {
        return repository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(NavigationMenuSummary::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public NavigationMenuForm formFor(Long id) {
        NavigationMenu menu = find(id);
        NavigationMenuForm form = new NavigationMenuForm();
        form.setMenuKey(menu.getMenuKey());
        form.setLabel(menu.getLabel());
        form.setMenuGroup(menu.getMenuGroup());
        form.setPath(menu.getPath());
        form.setIcon(menu.getIcon());
        form.setRequiredRole(menu.getRequiredRole());
        form.setDisplayOrder(menu.getDisplayOrder());
        form.setEnabled(menu.isEnabled());
        return form;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public NavigationMenuSummary create(NavigationMenuForm form) {
        validateLocalPath(form.getPath());
        if (repository.existsByMenuKey(form.getMenuKey())) {
            throw new NavigationMenuOperationException("이미 사용 중인 메뉴 키입니다.");
        }
        try {
            return NavigationMenuSummary.from(repository.saveAndFlush(NavigationMenu.create(form, clock.instant())));
        } catch (DataIntegrityViolationException exception) {
            throw new NavigationMenuOperationException("메뉴 키가 중복되었거나 저장값이 올바르지 않습니다.", exception);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public NavigationMenuSummary update(Long id, NavigationMenuForm form) {
        validateLocalPath(form.getPath());
        NavigationMenu menu = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new NavigationMenuOperationException("대상 메뉴를 찾을 수 없습니다."));
        if (repository.existsByMenuKeyAndIdNot(form.getMenuKey(), id)) {
            throw new NavigationMenuOperationException("이미 사용 중인 메뉴 키입니다.");
        }
        try {
            menu.update(form, clock.instant());
            repository.flush();
            return NavigationMenuSummary.from(menu);
        } catch (DataIntegrityViolationException exception) {
            throw new NavigationMenuOperationException("메뉴 키가 중복되었거나 저장값이 올바르지 않습니다.", exception);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public NavigationMenuSummary toggleEnabled(Long id) {
        NavigationMenu menu = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new NavigationMenuOperationException("대상 메뉴를 찾을 수 없습니다."));
        menu.toggleEnabled(clock.instant());
        return NavigationMenuSummary.from(menu);
    }

    private NavigationMenu find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NavigationMenuOperationException("대상 메뉴를 찾을 수 없습니다."));
    }

    private void validateLocalPath(String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//") || path.indexOf('\\') >= 0) {
            throw new NavigationMenuOperationException("메뉴 경로는 애플리케이션 내부 절대 경로여야 합니다.");
        }
        boolean reserved = workspaceProperties.reservedPaths().stream()
                .anyMatch(reservedPath -> path.equals(reservedPath) || path.startsWith(reservedPath + "/"));
        if (reserved) {
            throw new NavigationMenuOperationException("인증·업무 셸·내부 관리 경로는 메뉴 화면으로 등록할 수 없습니다.");
        }
        try {
            URI uri = new URI(path);
            if (uri.isAbsolute()
                    || uri.getRawAuthority() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null
                    || !uri.normalize().getPath().equals(path)) {
                throw new NavigationMenuOperationException("외부 주소, 쿼리, 경로 이동 문자는 메뉴에 사용할 수 없습니다.");
            }
        } catch (URISyntaxException exception) {
            throw new NavigationMenuOperationException("메뉴 경로 형식이 올바르지 않습니다.", exception);
        }
    }
}
