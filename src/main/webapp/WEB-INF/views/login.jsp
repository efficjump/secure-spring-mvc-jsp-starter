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
            <p class="eyebrow"><spring:message code="login.context.eyebrow"/></p>
            <h2><spring:message code="login.context.heading.first"/><br><spring:message code="login.context.heading.second"/></h2>
            <p><spring:message code="login.context.description"/></p>
        </div>
        <spring:message var="loginBaselineLabel" code="login.baseline.label"/>
        <div class="auth-baseline" aria-label="<c:out value="${loginBaselineLabel}"/>">
            <span><spring:message code="login.baseline.password"/></span>
            <span><spring:message code="login.baseline.csrf"/></span>
            <span><spring:message code="login.baseline.audit"/></span>
        </div>
    </div>
    <div class="auth-panel">
        <%@ include file="fragments/language-switcher.jspf" %>
        <div class="auth-form-wrap">
            <p class="eyebrow"><spring:message code="login.form.eyebrow"/></p>
            <h1><spring:message code="login.title"/></h1>
            <p class="auth-intro"><spring:message code="login.form.description"/></p>

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
            <p class="auth-help"><spring:message code="login.help"/></p>
        </div>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
