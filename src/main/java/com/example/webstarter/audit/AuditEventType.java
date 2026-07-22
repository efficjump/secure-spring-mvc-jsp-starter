package com.example.webstarter.audit;

public enum AuditEventType {
    LOGIN_SUCCESS("로그인 성공"),
    LOGIN_FAILURE("로그인 실패"),
    LOGIN_INPUT_REJECTED("로그인 입력 거부"),
    LOGIN_RATE_LIMITED("로그인 시도 제한"),
    LOGOUT("로그아웃"),
    REGISTRATION_SUCCESS("가입 성공"),
    REGISTRATION_FAILURE("가입 실패"),
    PASSWORD_CHANGED("비밀번호 변경"),
    USER_ENABLED("사용자 활성화"),
    USER_DISABLED("사용자 비활성화"),
    USER_UNLOCKED("사용자 잠금 해제"),
    USER_ROLE_CHANGED("사용자 권한 변경"),
    SESSION_TERMINATED("세션 종료"),
    MENU_CREATED("메뉴 생성"),
    MENU_UPDATED("메뉴 변경"),
    MENU_ENABLED("메뉴 표시"),
    MENU_DISABLED("메뉴 숨김"),
    ACCESS_DENIED("접근 거부"),
    BOOTSTRAP_ADMIN_CREATED("초기 관리자 생성");

    private final String displayName;

    AuditEventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
