<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="시작"/>
<%@ include file="fragments/header.jspf" %>

<section class="hero">
    <div>
        <p class="eyebrow">Production-oriented starter</p>
        <h1>안전한 기본값에서<br>서비스 개발을 시작하세요.</h1>
        <p class="lead">인증과 인가, 세션 보호, 감사 기록, 데이터베이스 마이그레이션과 쿼리 로그를 한 기준 안에 담았습니다.</p>
        <div class="button-row">
            <sec:authorize access="isAnonymous()">
                <c:url var="loginUrl" value="/login"/>
                <a class="button button-large" href="${loginUrl}">로그인</a>
            </sec:authorize>
            <sec:authorize access="isAuthenticated()">
                <c:url var="workspaceUrl" value="/workspace"/>
                <a class="button button-large" href="${workspaceUrl}">업무 공간 열기</a>
            </sec:authorize>
        </div>
    </div>
</section>

<section class="feature-grid" aria-label="기본 제공 기능">
    <article class="feature-card">
        <h2>인증 보호</h2>
        <p>Argon2 기반 비밀번호 저장, 점진적 재해시, 로그인 속도 제한과 계정 잠금을 제공합니다.</p>
    </article>
    <article class="feature-card">
        <h2>요청 보호</h2>
        <p>CSRF, 세션 고정 방어, 동시 세션 제한, CSP와 주요 보안 응답 헤더를 기본 적용합니다.</p>
    </article>
    <article class="feature-card">
        <h2>통합 업무 공간</h2>
        <p>다중 탭 셸에서 사용자, 로그인 이력, 활성 세션과 메뉴 구성을 한 흐름으로 관리합니다.</p>
    </article>
</section>

<%@ include file="fragments/footer.jspf" %>
