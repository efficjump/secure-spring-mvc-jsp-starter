<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="registration.title"/></c:set>
<%@ include file="fragments/header.jspf" %>

<section class="form-shell">
    <div class="form-card form-card-wide">
        <p class="eyebrow"><spring:message code="registration.eyebrow"/></p>
        <h1><spring:message code="registration.title"/></h1>
        <form:form method="post" modelAttribute="registrationForm">
            <form:errors path="*" cssClass="notice notice-error" element="div"/>
            <div class="field-grid">
                <div class="field">
                    <form:label path="username"><spring:message code="registration.username"/></form:label>
                    <form:input path="username" autocomplete="username" maxlength="64"/>
                    <form:errors path="username" cssClass="field-error"/>
                </div>
                <div class="field">
                    <form:label path="displayName"><spring:message code="registration.displayName"/></form:label>
                    <form:input path="displayName" autocomplete="name" maxlength="100"/>
                    <form:errors path="displayName" cssClass="field-error"/>
                </div>
            </div>
            <div class="field">
                <form:label path="email"><spring:message code="registration.email"/></form:label>
                <form:input path="email" type="email" autocomplete="email" maxlength="254"/>
                <form:errors path="email" cssClass="field-error"/>
            </div>
            <div class="field-grid">
                <div class="field">
                    <form:label path="password"><spring:message code="registration.password"/></form:label>
                    <form:password path="password" autocomplete="new-password" maxlength="${passwordMaxLength}"/>
                    <form:errors path="password" cssClass="field-error"/>
                </div>
                <div class="field">
                    <form:label path="passwordConfirmation"><spring:message code="registration.passwordConfirmation"/></form:label>
                    <form:password path="passwordConfirmation" autocomplete="new-password" maxlength="${passwordMaxLength}"/>
                    <form:errors path="passwordConfirmation" cssClass="field-error"/>
                </div>
            </div>
            <p class="help-text"><spring:message code="registration.help"/></p>
            <button class="button button-block" type="submit"><spring:message code="registration.submit"/></button>
        </form:form>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
