<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><spring:message code="workspace.title"/> · <spring:message code="app.name"/></title>
    <c:url var="stylesheetUrl" value="/assets/css/app.css"/>
    <c:url var="modernThemeUrl" value="/assets/css/theme-modern.css"/>
    <c:url var="workspaceScriptUrl" value="/assets/js/workspace.js"/>
    <link rel="stylesheet" href="${stylesheetUrl}">
    <link rel="stylesheet" href="${modernThemeUrl}">
    <script src="${workspaceScriptUrl}" defer></script>
</head>
<body class="workspace-body">
<a class="skip-link" href="#workspace-content">본문으로 바로가기</a>
<spring:message var="tabLimitMessage" code="workspace.tab.limit" arguments="${workspaceMaxTabs}"/>
<spring:message var="tabCloseLabel" code="workspace.tab.close"/>
<spring:message var="sessionExpiredMessage" code="workspace.session.expired"/>
<c:url var="iconSpriteUrl" value="/assets/icons/workspace-icons.svg"/>
<div id="workspace"
     class="workspace"
     data-default-menu-key="<c:out value="${workspaceDefaultMenuKey}"/>"
     data-max-tabs="<c:out value="${workspaceMaxTabs}"/>"
     data-storage-key="<c:out value="${workspaceStorageKey}"/>"
     data-user="<c:out value="${workspaceUser.username}"/>"
     data-tab-limit-message="<c:out value="${tabLimitMessage}"/>"
     data-tab-close-label="<c:out value="${tabCloseLabel}"/>"
     data-session-expired-message="<c:out value="${sessionExpiredMessage}"/>">
    <aside class="workspace-sidebar" aria-label="업무 메뉴">
        <div class="workspace-brand-row">
            <c:url var="workspaceHomeUrl" value="/workspace"/>
            <a class="workspace-brand" href="${workspaceHomeUrl}" aria-label="업무 공간 처음으로">
                <span class="brand-mark" aria-hidden="true">S</span>
                <span class="brand-copy">
                    <strong><spring:message code="app.name"/></strong>
                    <small>Enterprise Workspace</small>
                </span>
            </a>
            <button class="icon-button sidebar-toggle" type="button" data-action="toggle-sidebar" aria-label="메뉴 접기">
                <svg aria-hidden="true"><use href="${iconSpriteUrl}#icon-sidebar"></use></svg>
            </button>
        </div>

        <div class="menu-search-wrap">
            <svg aria-hidden="true"><use href="${iconSpriteUrl}#icon-search"></use></svg>
            <label class="visually-hidden" for="menu-search"><spring:message code="workspace.menu.search"/></label>
            <input id="menu-search" type="search" placeholder="<spring:message code="workspace.menu.search"/>" autocomplete="off">
        </div>

        <nav class="workspace-navigation" aria-label="애플리케이션 메뉴">
            <c:set var="currentMenuGroup" value=""/>
            <c:forEach var="menu" items="${menus}">
                <c:if test="${menu.menuGroup ne currentMenuGroup}">
                    <c:if test="${not empty currentMenuGroup}"></div></c:if>
                    <div class="menu-group" data-menu-group>
                    <p class="menu-group-title"><c:out value="${menu.menuGroup}"/></p>
                    <c:set var="currentMenuGroup" value="${menu.menuGroup}"/>
                </c:if>
                <c:url var="menuUrl" value="${menu.path}"/>
                <a class="workspace-menu-link"
                   href="${menuUrl}"
                   data-menu-key="<c:out value="${menu.menuKey}"/>"
                   data-menu-label="<c:out value="${menu.label}"/>"
                   data-menu-path="<c:out value="${menuUrl}"/>"
                   title="<c:out value="${menu.label}"/>">
                    <svg aria-hidden="true"><use href="${iconSpriteUrl}#icon-<c:out value="${menu.icon.symbolId}"/>"></use></svg>
                    <span><c:out value="${menu.label}"/></span>
                </a>
            </c:forEach>
            <c:if test="${not empty currentMenuGroup}"></div></c:if>
        </nav>

        <div class="workspace-profile">
            <span class="profile-avatar" aria-hidden="true"><c:out value="${workspaceUser.displayName.substring(0, 1)}"/></span>
            <span class="profile-copy">
                <strong><c:out value="${workspaceUser.displayName}"/></strong>
                <small><c:choose><c:when test="${workspaceUser.admin}">Administrator</c:when><c:otherwise>Standard user</c:otherwise></c:choose></small>
            </span>
        </div>
    </aside>

    <section class="workspace-main">
        <header class="workspace-topbar">
            <div class="topbar-context">
                <button class="icon-button mobile-menu-button" type="button" data-action="toggle-mobile-menu" aria-label="메뉴 열기">
                    <svg aria-hidden="true"><use href="${iconSpriteUrl}#icon-menu"></use></svg>
                </button>
                <span class="workspace-kicker"><spring:message code="workspace.title"/></span>
                <span class="topbar-divider" aria-hidden="true"></span>
                <strong id="active-view-title"><spring:message code="workspace.loading"/></strong>
            </div>
            <div class="topbar-actions">
                <span class="connection-state"><i aria-hidden="true"></i><spring:message code="workspace.connected"/></span>
                <button class="icon-button" type="button" data-action="refresh-tab" title="<spring:message code="workspace.refresh"/>" aria-label="<spring:message code="workspace.refresh"/>">
                    <svg aria-hidden="true"><use href="${iconSpriteUrl}#icon-refresh"></use></svg>
                </button>
                <c:url var="logoutUrl" value="/logout"/>
                <form class="inline-form" action="${logoutUrl}" method="post">
                    <sec:csrfInput/>
                    <button class="topbar-logout" type="submit"><spring:message code="nav.logout"/></button>
                </form>
            </div>
        </header>

        <div class="workspace-tabbar">
            <div id="workspace-tabs" class="workspace-tabs" role="tablist" aria-label="열린 업무 화면"></div>
            <button class="tabbar-action" type="button" data-action="close-other-tabs"><spring:message code="workspace.tabs.reset"/></button>
        </div>

        <main id="workspace-content" class="workspace-content">
            <div id="workspace-panels" class="workspace-panels"></div>
            <div id="workspace-empty" class="workspace-empty" hidden>
                <svg aria-hidden="true"><use href="${iconSpriteUrl}#icon-document"></use></svg>
                <strong><spring:message code="workspace.empty.title"/></strong>
                <p><spring:message code="workspace.empty.description"/></p>
            </div>
        </main>
    </section>
    <button class="sidebar-scrim" type="button" data-action="close-mobile-menu" aria-label="메뉴 닫기"></button>
    <div id="workspace-toast" class="workspace-toast" role="status" aria-live="polite" hidden></div>
</div>
</body>
</html>
