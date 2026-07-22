<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="pageTitle" value="세션 관리"/>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker">Session control</p>
        <h1>세션 관리</h1>
        <p>현재 애플리케이션 노드에서 인증된 세션을 확인하고 의심스러운 접속을 즉시 만료시킵니다.</p>
    </div>
    <span class="count-badge"><c:out value="${fn:length(sessions)}"/>개</span>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><c:out value="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><c:out value="${error}"/></div></c:if>

<div class="table-wrap">
    <table>
        <thead>
        <tr>
            <th scope="col">세션</th>
            <th scope="col">사용자</th>
            <th scope="col">접속 위치</th>
            <th scope="col">로그인 / 최근 활동</th>
            <th scope="col">최근 화면</th>
            <th scope="col">관리</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="session" items="${sessions}">
            <tr>
                <td>
                    <strong class="mono">#<c:out value="${session.fingerprint}"/></strong>
                    <c:choose>
                        <c:when test="${session.current}"><span class="status status-success">현재 세션</span></c:when>
                        <c:when test="${session.expired}"><span class="status status-muted">만료됨</span></c:when>
                        <c:otherwise><span class="status status-success">활성</span></c:otherwise>
                    </c:choose>
                </td>
                <td><strong><c:out value="${session.username}"/></strong></td>
                <td>
                    <span class="mono"><c:choose><c:when test="${empty session.ipAddress}">수집 전</c:when><c:otherwise><c:out value="${session.ipAddress}"/></c:otherwise></c:choose></span>
                    <span class="secondary truncate-cell" title="<c:out value="${session.userAgent}"/>"><c:out value="${session.userAgent}"/></span>
                </td>
                <td>
                    <span><c:choose><c:when test="${empty session.signedInAt}">수집 전</c:when><c:otherwise><c:out value="${session.signedInAt}"/></c:otherwise></c:choose></span>
                    <span class="secondary"><c:out value="${session.lastSeenAt}"/></span>
                </td>
                <td class="mono"><c:choose><c:when test="${empty session.lastPath}">-</c:when><c:otherwise><c:out value="${session.lastPath}"/></c:otherwise></c:choose></td>
                <td>
                    <c:if test="${not session.current and not session.expired}">
                        <c:url var="terminateUrl" value="/admin/sessions/terminate"/>
                        <form class="inline-form" action="${terminateUrl}" method="post">
                            <sec:csrfInput/>
                            <input type="hidden" name="sessionToken" value="<c:out value="${session.terminationToken}"/>">
                            <button class="text-button" type="submit">세션 종료</button>
                        </form>
                    </c:if>
                    <c:if test="${session.current}"><span class="secondary">로그아웃으로 종료</span></c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty sessions}"><tr><td colspan="6">표시할 세션이 없습니다.</td></tr></c:if>
        </tbody>
    </table>
</div>

<p class="help-text">세션 목록과 강제 종료는 단일 노드 기준입니다. 다중 인스턴스 운영에서는 Spring Session과 공유 저장소로 확장해야 합니다.</p>

<%@ include file="../fragments/footer.jspf" %>
