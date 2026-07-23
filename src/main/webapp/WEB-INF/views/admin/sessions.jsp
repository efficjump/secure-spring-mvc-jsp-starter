<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="admin.sessions.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="admin.sessions.kicker"/></p>
        <h1><spring:message code="admin.sessions.heading"/></h1>
        <p><spring:message code="admin.sessions.description"/></p>
    </div>
    <span class="count-badge"><spring:message code="admin.sessions.count" arguments="${fn:length(sessions)}"/></span>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><spring:message code="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><spring:message code="${error}"/></div></c:if>

<div class="table-wrap">
    <table>
        <thead>
        <tr>
            <th scope="col"><spring:message code="admin.sessions.column.session"/></th>
            <th scope="col"><spring:message code="admin.sessions.column.user"/></th>
            <th scope="col"><spring:message code="admin.sessions.column.location"/></th>
            <th scope="col"><spring:message code="admin.sessions.column.activity"/></th>
            <th scope="col"><spring:message code="admin.sessions.column.path"/></th>
            <th scope="col"><spring:message code="admin.sessions.column.actions"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="session" items="${sessions}">
            <tr>
                <td>
                    <strong class="mono">#<c:out value="${session.fingerprint}"/></strong>
                    <c:choose>
                        <c:when test="${session.current}"><span class="status status-success"><spring:message code="admin.sessions.current"/></span></c:when>
                        <c:when test="${session.expired}"><span class="status status-muted"><spring:message code="common.status.expired"/></span></c:when>
                        <c:otherwise><span class="status status-success"><spring:message code="common.status.active"/></span></c:otherwise>
                    </c:choose>
                </td>
                <td><strong><c:out value="${session.username}"/></strong></td>
                <td>
                    <span class="mono"><c:choose><c:when test="${empty session.ipAddress}"><spring:message code="common.notCollected"/></c:when><c:otherwise><c:out value="${session.ipAddress}"/></c:otherwise></c:choose></span>
                    <span class="secondary truncate-cell" title="<c:out value="${session.userAgent}"/>"><c:out value="${session.userAgent}"/></span>
                </td>
                <td>
                    <span><c:choose><c:when test="${empty session.signedInAt}"><spring:message code="common.notCollected"/></c:when><c:otherwise><c:out value="${session.signedInAt}"/></c:otherwise></c:choose></span>
                    <span class="secondary"><c:out value="${session.lastSeenAt}"/></span>
                </td>
                <td class="mono"><c:choose><c:when test="${empty session.lastPath}">-</c:when><c:otherwise><c:out value="${session.lastPath}"/></c:otherwise></c:choose></td>
                <td>
                    <c:if test="${not session.current and not session.expired}">
                        <c:url var="terminateUrl" value="/admin/sessions/terminate"/>
                        <form class="inline-form" action="${terminateUrl}" method="post">
                            <sec:csrfInput/>
                            <input type="hidden" name="sessionToken" value="<c:out value="${session.terminationToken}"/>">
                            <button class="text-button" type="submit"><spring:message code="admin.sessions.action.terminate"/></button>
                        </form>
                    </c:if>
                    <c:if test="${session.current}"><span class="secondary"><spring:message code="admin.sessions.action.logout"/></span></c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty sessions}"><tr><td colspan="6"><spring:message code="admin.sessions.empty"/></td></tr></c:if>
        </tbody>
    </table>
</div>

<p class="help-text"><spring:message code="admin.sessions.clusterNotice"/></p>

<%@ include file="../fragments/footer.jspf" %>
