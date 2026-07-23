<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="dashboard.title"/></c:set>
<%@ include file="fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="dashboard.kicker"/></p>
        <h1><spring:message code="dashboard.heading"/></h1>
        <p><spring:message code="dashboard.description" arguments="${user.displayName}"/></p>
    </div>
    <span class="status status-success"><spring:message code="common.status.healthy"/></span>
</section>

<c:choose>
    <c:when test="${operations.administrator}">
        <spring:message var="adminMetricsLabel" code="dashboard.admin.metrics.label"/>
        <section class="metric-strip" aria-label="<c:out value="${adminMetricsLabel}"/>">
            <div class="metric-item"><span class="metric-label"><spring:message code="dashboard.metric.totalUsers.label"/></span><strong class="metric-value"><c:out value="${operations.totalUsers}"/></strong><span class="metric-caption"><spring:message code="dashboard.metric.totalUsers.caption"/></span></div>
            <div class="metric-item"><span class="metric-label"><spring:message code="dashboard.metric.enabledUsers.label"/></span><strong class="metric-value"><c:out value="${operations.enabledUsers}"/></strong><span class="metric-caption"><spring:message code="dashboard.metric.enabledUsers.caption"/></span></div>
            <div class="metric-item"><span class="metric-label"><spring:message code="dashboard.metric.activeSessions.label"/></span><strong class="metric-value"><c:out value="${operations.activeSessions}"/></strong><span class="metric-caption"><spring:message code="dashboard.metric.activeSessions.caption"/></span></div>
            <div class="metric-item"><span class="metric-label"><spring:message code="dashboard.metric.rejectedLogins.label"/></span><strong class="metric-value"><c:out value="${operations.rejectedLoginsLast24Hours}"/></strong><span class="metric-caption"><spring:message code="dashboard.metric.rejectedLogins.caption"/></span></div>
        </section>
    </c:when>
    <c:otherwise>
        <spring:message var="userMetricsLabel" code="dashboard.user.metrics.label"/>
        <section class="metric-strip" aria-label="<c:out value="${userMetricsLabel}"/>">
            <div class="metric-item"><span class="metric-label"><spring:message code="dashboard.metric.accountStatus.label"/></span><strong class="metric-value"><spring:message code="common.status.active"/></strong><span class="metric-caption"><spring:message code="dashboard.metric.accountStatus.caption"/></span></div>
            <div class="metric-item"><span class="metric-label"><spring:message code="dashboard.metric.role.label"/></span><strong class="metric-value"><spring:message code="role.user"/></strong><span class="metric-caption"><spring:message code="dashboard.metric.role.caption"/></span></div>
            <div class="metric-item"><span class="metric-label"><spring:message code="dashboard.metric.lastLogin.label"/></span><strong class="metric-value"><c:choose><c:when test="${empty user.lastLoginAt}"><spring:message code="common.first"/></c:when><c:otherwise><spring:message code="common.completed"/></c:otherwise></c:choose></strong><span class="metric-caption"><c:out value="${user.lastLoginAt}"/></span></div>
            <div class="metric-item"><span class="metric-label"><spring:message code="dashboard.metric.security.label"/></span><strong class="metric-value"><spring:message code="common.status.applied"/></strong><span class="metric-caption"><spring:message code="dashboard.metric.security.caption"/></span></div>
        </section>
    </c:otherwise>
</c:choose>

<section class="content-section">
    <div class="section-heading"><h2><spring:message code="dashboard.account.title"/></h2><span class="secondary"><spring:message code="dashboard.account.caption"/></span></div>
    <dl class="definition-list">
        <div class="definition-row"><dt><spring:message code="dashboard.account.username"/></dt><dd><c:out value="${user.username}"/></dd></div>
        <div class="definition-row"><dt><spring:message code="dashboard.account.displayName"/></dt><dd><c:out value="${user.displayName}"/></dd></div>
        <div class="definition-row"><dt><spring:message code="dashboard.account.email"/></dt><dd><c:out value="${user.email}"/></dd></div>
        <div class="definition-row"><dt><spring:message code="dashboard.account.lastLogin"/></dt><dd><c:choose><c:when test="${empty user.lastLoginAt}"><spring:message code="dashboard.account.firstLogin"/></c:when><c:otherwise><c:out value="${user.lastLoginAt}"/></c:otherwise></c:choose></dd></div>
        <div class="definition-row"><dt><spring:message code="dashboard.account.createdAt"/></dt><dd><c:out value="${user.createdAt}"/></dd></div>
    </dl>
</section>

<%@ include file="fragments/footer.jspf" %>
