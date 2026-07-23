<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="admin.logins.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="admin.logins.kicker"/></p>
        <h1><spring:message code="admin.logins.heading"/></h1>
        <p><spring:message code="admin.logins.description"/></p>
    </div>
    <span class="count-badge"><spring:message code="admin.logins.count" arguments="${events.totalElements}"/></span>
</section>

<c:url var="filterUrl" value="/admin/logins"/>
<spring:message var="loginSearchLabel" code="admin.logins.search.label"/>
<spring:message var="loginSearchPlaceholder" code="admin.logins.search.placeholder"/>
<form class="filter-bar" method="get" action="${filterUrl}">
    <div>
        <label class="visually-hidden" for="login-search"><c:out value="${loginSearchLabel}"/></label>
        <input id="login-search" name="search" type="search" value="<c:out value="${search}"/>" placeholder="<c:out value="${loginSearchPlaceholder}"/>" maxlength="128">
    </div>
    <div>
        <label class="visually-hidden" for="event-type"><spring:message code="admin.logins.eventType.label"/></label>
        <select id="event-type" name="eventType">
            <option value=""><spring:message code="admin.logins.eventType.all"/></option>
            <c:forEach var="type" items="${eventTypes}">
                <option value="${type}" <c:if test="${selectedEventType eq type}">selected</c:if>><spring:message code="${type.messageCode}"/></option>
            </c:forEach>
        </select>
    </div>
    <div>
        <label class="visually-hidden" for="event-outcome"><spring:message code="admin.logins.outcome.label"/></label>
        <select id="event-outcome" name="outcome">
            <option value=""><spring:message code="admin.logins.outcome.all"/></option>
            <c:forEach var="item" items="${outcomes}">
                <option value="${item}" <c:if test="${selectedOutcome eq item}">selected</c:if>><spring:message code="${item.messageCode}"/></option>
            </c:forEach>
        </select>
    </div>
    <button class="button" type="submit"><spring:message code="admin.logins.filter"/></button>
</form>

<div class="table-wrap">
    <table>
        <thead>
        <tr>
            <th scope="col"><spring:message code="admin.logins.column.timestamp"/></th>
            <th scope="col"><spring:message code="admin.logins.column.event"/></th>
            <th scope="col"><spring:message code="admin.logins.column.account"/></th>
            <th scope="col"><spring:message code="admin.logins.column.connection"/></th>
            <th scope="col"><spring:message code="admin.logins.column.request"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="event" items="${events.content}">
            <tr>
                <td class="mono"><c:out value="${event.createdAt}"/></td>
                <td>
                    <strong><spring:message code="${event.eventType.messageCode}"/></strong>
                    <c:choose>
                        <c:when test="${event.outcome eq 'SUCCESS'}"><span class="status status-success"><spring:message code="${event.outcome.messageCode}"/></span></c:when>
                        <c:when test="${event.outcome eq 'FAILURE'}"><span class="status status-danger"><spring:message code="${event.outcome.messageCode}"/></span></c:when>
                        <c:otherwise><span class="status status-warning"><spring:message code="${event.outcome.messageCode}"/></span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <strong><c:choose><c:when test="${empty event.actorUsername}"><spring:message code="admin.logins.unknownActor"/></c:when><c:otherwise><c:out value="${event.actorUsername}"/></c:otherwise></c:choose></strong>
                    <c:if test="${not empty event.subject and event.subject ne event.actorUsername}"><span class="secondary"><c:out value="${event.subject}"/></span></c:if>
                </td>
                <td>
                    <span class="mono"><c:choose><c:when test="${empty event.ipAddress}">-</c:when><c:otherwise><c:out value="${event.ipAddress}"/></c:otherwise></c:choose></span>
                    <span class="secondary truncate-cell" title="<c:out value="${event.userAgent}"/>"><c:out value="${event.userAgent}"/></span>
                </td>
                <td>
                    <span class="mono"><c:choose><c:when test="${empty event.requestId}">-</c:when><c:otherwise><c:out value="${event.requestId}"/></c:otherwise></c:choose></span>
                    <c:if test="${not empty event.detail}"><span class="secondary"><c:out value="${event.detail}"/></span></c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty events.content}">
            <tr><td colspan="5"><spring:message code="admin.logins.empty"/></td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<spring:message var="loginsPaginationLabel" code="admin.logins.pagination"/>
<c:set var="loginsPageNumber" value="${events.number + 1}"/>
<c:set var="loginsTotalPages" value="${events.totalPages == 0 ? 1 : events.totalPages}"/>
<nav class="pagination" aria-label="<c:out value="${loginsPaginationLabel}"/>">
    <span><spring:message code="common.pageIndicator" arguments="${loginsPageNumber},${loginsTotalPages}"/></span>
    <div class="pagination-links">
        <c:if test="${events.hasPrevious()}">
            <c:url var="previousUrl" value="/admin/logins">
                <c:param name="page" value="${events.number - 1}"/><c:param name="search" value="${search}"/>
                <c:if test="${not empty selectedEventType}"><c:param name="eventType" value="${selectedEventType}"/></c:if>
                <c:if test="${not empty selectedOutcome}"><c:param name="outcome" value="${selectedOutcome}"/></c:if>
            </c:url>
            <a class="button button-quiet" href="${previousUrl}"><spring:message code="common.previous"/></a>
        </c:if>
        <c:if test="${events.hasNext()}">
            <c:url var="nextUrl" value="/admin/logins">
                <c:param name="page" value="${events.number + 1}"/><c:param name="search" value="${search}"/>
                <c:if test="${not empty selectedEventType}"><c:param name="eventType" value="${selectedEventType}"/></c:if>
                <c:if test="${not empty selectedOutcome}"><c:param name="outcome" value="${selectedOutcome}"/></c:if>
            </c:url>
            <a class="button button-quiet" href="${nextUrl}"><spring:message code="common.next"/></a>
        </c:if>
    </div>
</nav>

<%@ include file="../fragments/footer.jspf" %>
