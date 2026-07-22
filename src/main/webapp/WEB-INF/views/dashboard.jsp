<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="대시보드"/>
<%@ include file="fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker">Operations overview</p>
        <h1>업무 현황</h1>
        <p><c:out value="${user.displayName}"/>님의 계정 상태와 현재 운영 지표를 확인합니다.</p>
    </div>
    <span class="status status-success">정상 운영</span>
</section>

<c:choose>
    <c:when test="${operations.administrator}">
        <section class="metric-strip" aria-label="운영 지표">
            <div class="metric-item"><span class="metric-label">전체 사용자</span><strong class="metric-value"><c:out value="${operations.totalUsers}"/></strong><span class="metric-caption">등록 계정</span></div>
            <div class="metric-item"><span class="metric-label">활성 계정</span><strong class="metric-value"><c:out value="${operations.enabledUsers}"/></strong><span class="metric-caption">로그인 허용</span></div>
            <div class="metric-item"><span class="metric-label">활성 세션</span><strong class="metric-value"><c:out value="${operations.activeSessions}"/></strong><span class="metric-caption">현재 노드 기준</span></div>
            <div class="metric-item"><span class="metric-label">거부된 로그인</span><strong class="metric-value"><c:out value="${operations.rejectedLoginsLast24Hours}"/></strong><span class="metric-caption">최근 24시간</span></div>
        </section>
    </c:when>
    <c:otherwise>
        <section class="metric-strip" aria-label="계정 요약">
            <div class="metric-item"><span class="metric-label">계정 상태</span><strong class="metric-value">활성</strong><span class="metric-caption">로그인 허용</span></div>
            <div class="metric-item"><span class="metric-label">권한</span><strong class="metric-value">사용자</strong><span class="metric-caption">표준 업무 권한</span></div>
            <div class="metric-item"><span class="metric-label">최근 로그인</span><strong class="metric-value"><c:choose><c:when test="${empty user.lastLoginAt}">처음</c:when><c:otherwise>완료</c:otherwise></c:choose></strong><span class="metric-caption"><c:out value="${user.lastLoginAt}"/></span></div>
            <div class="metric-item"><span class="metric-label">보안 정책</span><strong class="metric-value">적용</strong><span class="metric-caption">세션·감사 보호</span></div>
        </section>
    </c:otherwise>
</c:choose>

<section class="content-section">
    <div class="section-heading"><h2>내 계정 정보</h2><span class="secondary">인증된 계정 기준</span></div>
    <dl class="definition-list">
        <div class="definition-row"><dt>사용자 이름</dt><dd><c:out value="${user.username}"/></dd></div>
        <div class="definition-row"><dt>표시 이름</dt><dd><c:out value="${user.displayName}"/></dd></div>
        <div class="definition-row"><dt>이메일</dt><dd><c:out value="${user.email}"/></dd></div>
        <div class="definition-row"><dt>마지막 로그인</dt><dd><c:choose><c:when test="${empty user.lastLoginAt}">첫 로그인</c:when><c:otherwise><c:out value="${user.lastLoginAt}"/></c:otherwise></c:choose></dd></div>
        <div class="definition-row"><dt>가입 일시</dt><dd><c:out value="${user.createdAt}"/></dd></div>
    </dl>
</section>

<%@ include file="fragments/footer.jspf" %>
