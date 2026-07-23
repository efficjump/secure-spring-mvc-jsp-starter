<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="password.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="password.kicker"/></p>
        <h1><spring:message code="password.title"/></h1>
        <p><spring:message code="password.description"/></p>
    </div>
    <span class="status status-success"><spring:message code="password.protected"/></span>
</section>

<section class="content-section">
    <div class="form-surface">
        <p class="help-text"><spring:message code="password.help"/></p>
        <form:form method="post" modelAttribute="passwordChangeForm">
            <form:errors path="*" cssClass="notice notice-error" element="div"/>
            <div class="field">
                <form:label path="currentPassword"><spring:message code="password.current"/></form:label>
                <form:password path="currentPassword" autocomplete="current-password" maxlength="${passwordMaxLength}"/>
                <form:errors path="currentPassword" cssClass="field-error"/>
            </div>
            <div class="field">
                <form:label path="newPassword"><spring:message code="password.new"/></form:label>
                <form:password path="newPassword" autocomplete="new-password" maxlength="${passwordMaxLength}"/>
                <form:errors path="newPassword" cssClass="field-error"/>
            </div>
            <div class="field">
                <form:label path="newPasswordConfirmation"><spring:message code="password.newConfirmation"/></form:label>
                <form:password path="newPasswordConfirmation" autocomplete="new-password" maxlength="${passwordMaxLength}"/>
                <form:errors path="newPasswordConfirmation" cssClass="field-error"/>
            </div>
            <div class="form-actions">
                <button class="button" type="submit"><spring:message code="password.submit"/></button>
            </div>
        </form:form>
    </div>
</section>

<%@ include file="../fragments/footer.jspf" %>
