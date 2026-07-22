<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="로그인 이력"/>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker">Authentication audit</p>
        <h1>로그인 이력</h1>
        <p>성공·실패·입력 거부·속도 제한과 로그아웃 기록을 요청 ID까지 연결해 조회합니다.</p>
    </div>
    <span class="count-badge"><c:out value="${events.totalElements}"/>건</span>
</section>

<c:url var="filterUrl" value="/admin/logins"/>
<form class="filter-bar" method="get" action="${filterUrl}">
    <div>
        <label class="visually-hidden" for="login-search">계정, IP 또는 요청 ID 검색</label>
        <input id="login-search" name="search" type="search" value="<c:out value="${search}"/>" placeholder="계정, IP 또는 요청 ID" maxlength="128">
    </div>
    <div>
        <label class="visually-hidden" for="event-type">이벤트 유형</label>
        <select id="event-type" name="eventType">
            <option value="">전체 이벤트</option>
            <c:forEach var="type" items="${eventTypes}">
                <option value="${type}" <c:if test="${selectedEventType eq type}">selected</c:if>><c:out value="${type.displayName}"/></option>
            </c:forEach>
        </select>
    </div>
    <div>
        <label class="visually-hidden" for="event-outcome">처리 결과</label>
        <select id="event-outcome" name="outcome">
            <option value="">전체 결과</option>
            <c:forEach var="item" items="${outcomes}">
                <option value="${item}" <c:if test="${selectedOutcome eq item}">selected</c:if>><c:out value="${item.displayName}"/></option>
            </c:forEach>
        </select>
    </div>
    <button class="button" type="submit">조회</button>
</form>

<div class="table-wrap">
    <table>
        <thead>
        <tr>
            <th scope="col">일시</th>
            <th scope="col">이벤트 / 결과</th>
            <th scope="col">계정</th>
            <th scope="col">접속 정보</th>
            <th scope="col">요청 추적</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="event" items="${events.content}">
            <tr>
                <td class="mono"><c:out value="${event.createdAt}"/></td>
                <td>
                    <strong><c:out value="${event.eventLabel}"/></strong>
                    <c:choose>
                        <c:when test="${event.outcome eq 'SUCCESS'}"><span class="status status-success"><c:out value="${event.outcomeLabel}"/></span></c:when>
                        <c:when test="${event.outcome eq 'FAILURE'}"><span class="status status-danger"><c:out value="${event.outcomeLabel}"/></span></c:when>
                        <c:otherwise><span class="status status-warning"><c:out value="${event.outcomeLabel}"/></span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <strong><c:choose><c:when test="${empty event.actorUsername}">식별 전</c:when><c:otherwise><c:out value="${event.actorUsername}"/></c:otherwise></c:choose></strong>
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
            <tr><td colspan="5">조건에 맞는 로그인 이력이 없습니다.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<nav class="pagination" aria-label="로그인 이력 페이지">
    <span><c:out value="${events.number + 1}"/> / <c:out value="${events.totalPages == 0 ? 1 : events.totalPages}"/> 페이지</span>
    <div class="pagination-links">
        <c:if test="${events.hasPrevious()}">
            <c:url var="previousUrl" value="/admin/logins">
                <c:param name="page" value="${events.number - 1}"/><c:param name="search" value="${search}"/>
                <c:if test="${not empty selectedEventType}"><c:param name="eventType" value="${selectedEventType}"/></c:if>
                <c:if test="${not empty selectedOutcome}"><c:param name="outcome" value="${selectedOutcome}"/></c:if>
            </c:url>
            <a class="button button-quiet" href="${previousUrl}">이전</a>
        </c:if>
        <c:if test="${events.hasNext()}">
            <c:url var="nextUrl" value="/admin/logins">
                <c:param name="page" value="${events.number + 1}"/><c:param name="search" value="${search}"/>
                <c:if test="${not empty selectedEventType}"><c:param name="eventType" value="${selectedEventType}"/></c:if>
                <c:if test="${not empty selectedOutcome}"><c:param name="outcome" value="${selectedOutcome}"/></c:if>
            </c:url>
            <a class="button button-quiet" href="${nextUrl}">다음</a>
        </c:if>
    </div>
</nav>

<%@ include file="../fragments/footer.jspf" %>
