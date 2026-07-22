<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="login.title"/></c:set>
<c:set var="immersivePage" value="true"/>
<%@ include file="fragments/header.jspf" %>

<section class="auth-layout">
    <div class="auth-context">
        <c:url var="homeUrl" value="/"/>
        <a class="auth-brand" href="${homeUrl}">
            <span class="auth-brand-mark" aria-hidden="true">S</span>
            <spring:message code="app.name"/>
        </a>
        <div class="auth-copy">
            <p class="eyebrow">Secure business platform</p>
            <h2>업무와 보안을<br>한 화면에서 관리합니다.</h2>
            <p>사용자 계정, 로그인 이력, 활성 세션과 시스템 메뉴를 안전한 다중 탭 작업공간에서 운영하세요.</p>
        </div>
        <div class="auth-baseline" aria-label="보안 기준">
            <span>Argon2</span>
            <span>CSRF protected</span>
            <span>Audit ready</span>
        </div>
    </div>
    <div class="auth-panel">
        <div class="auth-form-wrap">
            <p class="eyebrow">Account access</p>
            <h1><spring:message code="login.title"/></h1>
            <p class="auth-intro">등록된 업무 계정으로 보안 작업공간에 접속합니다.</p>

            <c:if test="${param.error != null}"><div class="notice notice-error" role="alert"><spring:message code="login.error"/></div></c:if>
            <c:if test="${param.rate_limited != null}"><div class="notice notice-error" role="alert"><spring:message code="login.rateLimited"/></div></c:if>
            <c:if test="${param.logout != null}"><div class="notice notice-success"><spring:message code="login.loggedOut"/></div></c:if>
            <c:if test="${param.expired != null}"><div class="notice notice-info"><spring:message code="login.expired"/></div></c:if>
            <c:if test="${param.password_changed != null}"><div class="notice notice-success"><spring:message code="login.passwordChanged"/></div></c:if>
            <c:if test="${param.registered != null}"><div class="notice notice-success"><spring:message code="login.registered"/></div></c:if>

            <c:url var="loginAction" value="/login"/>
            <form action="${loginAction}" method="post">
                <sec:csrfInput/>
                <div class="field">
                    <label for="username"><spring:message code="login.username"/></label>
                    <input id="username" name="username" type="text" autocomplete="username" maxlength="64" required autofocus>
                </div>
                <div class="field">
                    <label for="password"><spring:message code="login.password"/></label>
                    <input id="password" name="password" type="password" autocomplete="current-password" maxlength="${passwordMaxLength}" required>
                </div>
                <button class="button button-block" type="submit"><spring:message code="login.submit"/></button>
            </form>
            <p class="auth-help">계정 접근 문제는 시스템 관리자에게 문의해 주세요.</p>
        </div>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
