package com.example.webstarter.admin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.persistence.criteria.Predicate;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.SecurityAuditEventRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdminLoginAuditService {

    private static final int SEARCH_MAX_LENGTH = 128;
    private static final Set<AuditEventType> LOGIN_EVENT_TYPES = EnumSet.of(
            AuditEventType.LOGIN_SUCCESS,
            AuditEventType.LOGIN_FAILURE,
            AuditEventType.LOGIN_INPUT_REJECTED,
            AuditEventType.LOGIN_RATE_LIMITED,
            AuditEventType.LOGOUT);

    private final SecurityAuditEventRepository repository;

    public AdminLoginAuditService(SecurityAuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<LoginAuditSummary> list(
            int page,
            int pageSize,
            AuditEventType requestedEventType,
            AuditOutcome outcome,
            String rawSearch) {
        AuditEventType eventType = LOGIN_EVENT_TYPES.contains(requestedEventType)
                ? requestedEventType
                : null;
        String search = normalizeSearch(rawSearch);
        PageRequest pageable = PageRequest.of(
                Math.max(0, page),
                pageSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        return repository.findAll((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("eventType").in(LOGIN_EVENT_TYPES));
            if (eventType != null) {
                predicates.add(builder.equal(root.get("eventType"), eventType));
            }
            if (outcome != null) {
                predicates.add(builder.equal(root.get("outcome"), outcome));
            }
            if (!search.isEmpty()) {
                String pattern = "%" + escapeLike(search) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.<String>get("actorUsername")), pattern, '\\'),
                        builder.like(builder.lower(root.<String>get("subject")), pattern, '\\'),
                        builder.like(builder.lower(root.<String>get("ipAddress")), pattern, '\\'),
                        builder.like(builder.lower(root.<String>get("requestId")), pattern, '\\')));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        }, pageable).map(LoginAuditSummary::from);
    }

    public List<AuditEventType> supportedEventTypes() {
        return LOGIN_EVENT_TYPES.stream().toList();
    }

    private String normalizeSearch(String rawSearch) {
        if (rawSearch == null || rawSearch.isBlank()) {
            return "";
        }
        String clean = rawSearch.replace('\r', ' ').replace('\n', ' ').strip().toLowerCase(Locale.ROOT);
        return clean.length() <= SEARCH_MAX_LENGTH ? clean : clean.substring(0, SEARCH_MAX_LENGTH);
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
