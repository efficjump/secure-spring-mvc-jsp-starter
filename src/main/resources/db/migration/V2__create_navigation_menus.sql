CREATE TABLE navigation_menus (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_key VARCHAR(40) NOT NULL,
    label VARCHAR(80) NOT NULL,
    menu_group VARCHAR(60) NOT NULL,
    path VARCHAR(180) NOT NULL,
    icon VARCHAR(32) NOT NULL,
    required_role VARCHAR(32) NOT NULL,
    display_order INT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_navigation_menus_key UNIQUE (menu_key),
    CONSTRAINT ck_navigation_menus_role CHECK (required_role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_navigation_menus_order CHECK (display_order BETWEEN 0 AND 9999),
    INDEX ix_navigation_menus_enabled_order (enabled, display_order, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO navigation_menus
    (menu_key, label, menu_group, path, icon, required_role, display_order, enabled, created_at, updated_at, version)
VALUES
    ('dashboard', '업무 현황', '워크스페이스', '/dashboard', 'DASHBOARD', 'USER', 100, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0),
    ('user-accounts', '사용자 계정', '계정 및 접근', '/admin/users', 'USERS', 'ADMIN', 200, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0),
    ('login-history', '로그인 이력', '계정 및 접근', '/admin/logins', 'LOGIN_HISTORY', 'ADMIN', 210, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0),
    ('active-sessions', '세션 관리', '계정 및 접근', '/admin/sessions', 'SESSIONS', 'ADMIN', 220, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0),
    ('menu-management', '메뉴 관리', '시스템 설정', '/admin/menus', 'MENUS', 'ADMIN', 300, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0),
    ('password-security', '비밀번호 변경', '내 계정', '/account/password', 'SECURITY', 'USER', 400, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0);
