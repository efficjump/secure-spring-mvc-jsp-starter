<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="admin.users.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="admin.users.kicker"/></p>
        <h1><spring:message code="admin.users.heading"/></h1>
        <p><spring:message code="admin.users.description"/></p>
    </div>
    <span class="count-badge"><spring:message code="admin.users.count" arguments="${users.totalElements}"/></span>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><spring:message code="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><spring:message code="${error}"/></div></c:if>

<div class="table-wrap">
    <table>
        <thead>
        <tr>
            <th scope="col"><spring:message code="admin.users.column.account"/></th>
            <th scope="col"><spring:message code="admin.users.column.status"/></th>
            <th scope="col"><spring:message code="admin.users.column.role"/></th>
            <th scope="col"><spring:message code="admin.users.column.activity"/></th>
            <th scope="col"><spring:message code="admin.users.column.actions"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="user" items="${users.content}">
            <tr>
                <td>
                    <strong><c:out value="${user.displayName}"/></strong>
                    <span class="secondary"><c:out value="${user.username}"/> · <c:out value="${user.email}"/></span>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${not user.enabled}"><span class="status status-muted"><spring:message code="common.status.inactive"/></span></c:when>
                        <c:when test="${user.locked}"><span class="status status-warning"><spring:message code="common.status.locked"/></span></c:when>
                        <c:otherwise><span class="status status-success"><spring:message code="common.status.active"/></span></c:otherwise>
                    </c:choose>
                </td>
                <td><c:choose><c:when test="${user.admin}"><spring:message code="role.admin"/></c:when><c:otherwise><spring:message code="role.user"/></c:otherwise></c:choose></td>
                <td>
                    <c:choose><c:when test="${empty user.lastLoginAt}"><spring:message code="admin.users.noLogin"/></c:when><c:otherwise><c:out value="${user.lastLoginAt}"/></c:otherwise></c:choose>
                    <span class="secondary"><spring:message code="admin.users.joinedAt" arguments="${user.createdAt}"/></span>
                </td>
                <td>
                    <div class="action-stack">
                        <c:url var="toggleUrl" value="/admin/users/${user.id}/toggle-enabled"/>
                        <form class="inline-form" action="${toggleUrl}" method="post">
                            <sec:csrfInput/>
                            <button class="text-button" type="submit"><c:choose><c:when test="${user.enabled}"><spring:message code="admin.users.action.disable"/></c:when><c:otherwise><spring:message code="admin.users.action.enable"/></c:otherwise></c:choose></button>
                        </form>
                        <c:if test="${user.locked}">
                            <c:url var="unlockUrl" value="/admin/users/${user.id}/unlock"/>
                            <form class="inline-form" action="${unlockUrl}" method="post">
                                <sec:csrfInput/>
                                <button class="text-button" type="submit"><spring:message code="admin.users.action.unlock"/></button>
                            </form>
                        </c:if>
                        <c:url var="roleUrl" value="/admin/users/${user.id}/role"/>
                        <form class="inline-form role-form" action="${roleUrl}" method="post">
                            <sec:csrfInput/>
                            <label class="visually-hidden" for="role-${user.id}"><spring:message code="admin.users.role.label"/></label>
                            <select id="role-${user.id}" name="role">
                                <option value="USER" <c:if test="${not user.admin}">selected</c:if>><spring:message code="role.user"/></option>
                                <option value="ADMIN" <c:if test="${user.admin}">selected</c:if>><spring:message code="role.admin"/></option>
                            </select>
                            <button class="text-button" type="submit"><spring:message code="admin.users.action.changeRole"/></button>
                        </form>
                    </div>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty users.content}">
            <tr><td colspan="5"><spring:message code="admin.users.empty"/></td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<spring:message var="usersPaginationLabel" code="admin.users.pagination"/>
<c:set var="usersPageNumber" value="${users.number + 1}"/>
<c:set var="usersTotalPages" value="${users.totalPages == 0 ? 1 : users.totalPages}"/>
<nav class="pagination" aria-label="<c:out value="${usersPaginationLabel}"/>">
    <span><spring:message code="common.pageIndicator" arguments="${usersPageNumber},${usersTotalPages}"/></span>
    <div class="pagination-links">
        <c:if test="${users.hasPrevious()}">
            <c:url var="previousUrl" value="/admin/users"><c:param name="page" value="${users.number - 1}"/></c:url>
            <a class="button button-quiet" href="${previousUrl}"><spring:message code="common.previous"/></a>
        </c:if>
        <c:if test="${users.hasNext()}">
            <c:url var="nextUrl" value="/admin/users"><c:param name="page" value="${users.number + 1}"/></c:url>
            <a class="button button-quiet" href="${nextUrl}"><spring:message code="common.next"/></a>
        </c:if>
    </div>
</nav>

<%@ include file="../fragments/footer.jspf" %>
