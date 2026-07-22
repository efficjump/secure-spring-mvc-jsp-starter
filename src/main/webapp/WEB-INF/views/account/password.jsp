<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="password.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker">Account security</p>
        <h1><spring:message code="password.title"/></h1>
        <p>현재 자격 증명을 확인한 뒤 새 비밀번호를 설정합니다.</p>
    </div>
    <span class="status status-success">Argon2 보호</span>
</section>

<section class="content-section">
    <div class="form-surface">
        <p class="help-text">변경이 완료되면 탈취된 세션의 재사용을 막기 위해 현재 계정을 포함한 모든 세션을 종료합니다.</p>
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
