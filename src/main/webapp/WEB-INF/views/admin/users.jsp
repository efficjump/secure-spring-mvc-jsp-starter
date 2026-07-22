<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="pageTitle" value="사용자 관리"/>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker">Identity administration</p>
        <h1>사용자 계정</h1>
        <p>계정 상태, 로그인 잠금과 관리자 권한을 운영합니다. 모든 변경은 보안 감사 이벤트로 기록됩니다.</p>
    </div>
    <span class="count-badge"><c:out value="${users.totalElements}"/>명</span>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><spring:message code="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><c:out value="${error}"/></div></c:if>

<div class="table-wrap">
    <table>
        <thead>
        <tr>
            <th scope="col">계정</th>
            <th scope="col">상태</th>
            <th scope="col">권한</th>
            <th scope="col">최근 로그인 / 가입</th>
            <th scope="col">관리</th>
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
                        <c:when test="${not user.enabled}"><span class="status status-muted">비활성</span></c:when>
                        <c:when test="${user.locked}"><span class="status status-warning">잠김</span></c:when>
                        <c:otherwise><span class="status status-success">활성</span></c:otherwise>
                    </c:choose>
                </td>
                <td><c:choose><c:when test="${user.admin}">관리자</c:when><c:otherwise>사용자</c:otherwise></c:choose></td>
                <td>
                    <c:choose><c:when test="${empty user.lastLoginAt}">로그인 이력 없음</c:when><c:otherwise><c:out value="${user.lastLoginAt}"/></c:otherwise></c:choose>
                    <span class="secondary">가입 <c:out value="${user.createdAt}"/></span>
                </td>
                <td>
                    <div class="action-stack">
                        <c:url var="toggleUrl" value="/admin/users/${user.id}/toggle-enabled"/>
                        <form class="inline-form" action="${toggleUrl}" method="post">
                            <sec:csrfInput/>
                            <button class="text-button" type="submit"><c:choose><c:when test="${user.enabled}">비활성화</c:when><c:otherwise>활성화</c:otherwise></c:choose></button>
                        </form>
                        <c:if test="${user.locked}">
                            <c:url var="unlockUrl" value="/admin/users/${user.id}/unlock"/>
                            <form class="inline-form" action="${unlockUrl}" method="post">
                                <sec:csrfInput/>
                                <button class="text-button" type="submit">잠금 해제</button>
                            </form>
                        </c:if>
                        <c:url var="roleUrl" value="/admin/users/${user.id}/role"/>
                        <form class="inline-form role-form" action="${roleUrl}" method="post">
                            <sec:csrfInput/>
                            <label class="visually-hidden" for="role-${user.id}">권한</label>
                            <select id="role-${user.id}" name="role">
                                <option value="USER" <c:if test="${not user.admin}">selected</c:if>>사용자</option>
                                <option value="ADMIN" <c:if test="${user.admin}">selected</c:if>>관리자</option>
                            </select>
                            <button class="text-button" type="submit">변경</button>
                        </form>
                    </div>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<nav class="pagination" aria-label="사용자 목록 페이지">
    <span><c:out value="${users.number + 1}"/> / <c:out value="${users.totalPages == 0 ? 1 : users.totalPages}"/> 페이지</span>
    <div class="pagination-links">
        <c:if test="${users.hasPrevious()}">
            <c:url var="previousUrl" value="/admin/users"><c:param name="page" value="${users.number - 1}"/></c:url>
            <a class="button button-quiet" href="${previousUrl}">이전</a>
        </c:if>
        <c:if test="${users.hasNext()}">
            <c:url var="nextUrl" value="/admin/users"><c:param name="page" value="${users.number + 1}"/></c:url>
            <a class="button button-quiet" href="${nextUrl}">다음</a>
        </c:if>
    </div>
</nav>

<%@ include file="../fragments/footer.jspf" %>
