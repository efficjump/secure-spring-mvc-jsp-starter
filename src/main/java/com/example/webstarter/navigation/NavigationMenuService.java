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
            throw new NavigationMenuOperationException("navigation.error.duplicateKey");
        }
        try {
            return NavigationMenuSummary.from(repository.saveAndFlush(NavigationMenu.create(form, clock.instant())));
        } catch (DataIntegrityViolationException exception) {
            throw new NavigationMenuOperationException("navigation.error.invalidStoredValue", exception);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public NavigationMenuSummary update(Long id, NavigationMenuForm form) {
        validateLocalPath(form.getPath());
        NavigationMenu menu = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new NavigationMenuOperationException("navigation.error.notFound"));
        if (repository.existsByMenuKeyAndIdNot(form.getMenuKey(), id)) {
            throw new NavigationMenuOperationException("navigation.error.duplicateKey");
        }
        try {
            menu.update(form, clock.instant());
            repository.flush();
            return NavigationMenuSummary.from(menu);
        } catch (DataIntegrityViolationException exception) {
            throw new NavigationMenuOperationException("navigation.error.invalidStoredValue", exception);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public NavigationMenuSummary toggleEnabled(Long id) {
        NavigationMenu menu = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new NavigationMenuOperationException("navigation.error.notFound"));
        menu.toggleEnabled(clock.instant());
        return NavigationMenuSummary.from(menu);
    }

    private NavigationMenu find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NavigationMenuOperationException("navigation.error.notFound"));
    }

    private void validateLocalPath(String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//") || path.indexOf('\\') >= 0) {
            throw new NavigationMenuOperationException("navigation.error.path.absolute");
        }
        boolean reserved = workspaceProperties.reservedPaths().stream()
                .anyMatch(reservedPath -> path.equals(reservedPath) || path.startsWith(reservedPath + "/"));
        if (reserved) {
            throw new NavigationMenuOperationException("navigation.error.path.reserved");
        }
        try {
            URI uri = new URI(path);
            if (uri.isAbsolute()
                    || uri.getRawAuthority() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null
                    || !uri.normalize().getPath().equals(path)) {
                throw new NavigationMenuOperationException("navigation.error.path.unsafe");
            }
        } catch (URISyntaxException exception) {
            throw new NavigationMenuOperationException("navigation.error.path.invalid", exception);
        }
    }
}
