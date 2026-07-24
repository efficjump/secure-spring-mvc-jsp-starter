<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="admin.locales.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="admin.locales.kicker"/></p>
        <h1><spring:message code="admin.locales.heading"/></h1>
        <p><spring:message code="admin.locales.description"/></p>
    </div>
    <c:url var="newLocaleUrl" value="/admin/locales"/>
    <a class="button button-quiet" href="${newLocaleUrl}"><spring:message code="admin.locales.new"/></a>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><spring:message code="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><spring:message code="${error}"/></div></c:if>

<div class="editor-layout">
    <div class="table-wrap">
        <table>
            <thead>
            <tr>
                <th scope="col"><spring:message code="admin.locales.column.order"/></th>
                <th scope="col"><spring:message code="admin.locales.column.language"/></th>
                <th scope="col"><spring:message code="admin.locales.column.status"/></th>
                <th scope="col"><spring:message code="admin.locales.column.actions"/></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="locale" items="${locales}">
                <tr <c:if test="${editingLocaleId eq locale.id}">class="selected-row"</c:if>>
                    <td class="mono"><c:out value="${locale.displayOrder}"/></td>
                    <td>
                        <strong><c:out value="${locale.nativeName}"/></strong>
                        <span class="secondary"><c:out value="${locale.displayName}"/> · <span class="mono"><c:out value="${locale.languageTag}"/></span></span>
                    </td>
                    <td>
                        <div class="status-list">
                            <c:choose>
                                <c:when test="${locale.enabled}"><span class="status status-success"><spring:message code="common.status.active"/></span></c:when>
                                <c:otherwise><span class="status status-muted"><spring:message code="common.status.inactive"/></span></c:otherwise>
                            </c:choose>
                            <c:if test="${locale.defaultLocale}"><span class="status status-info"><spring:message code="admin.locales.default"/></span></c:if>
                        </div>
                    </td>
                    <td>
                        <div class="action-stack">
                            <c:url var="editUrl" value="/admin/locales"><c:param name="edit" value="${locale.id}"/></c:url>
                            <a class="text-button" href="${editUrl}"><spring:message code="admin.locales.action.edit"/></a>
                            <c:url var="translationsUrl" value="/admin/locales/${locale.id}/messages"/>
                            <a class="text-button" href="${translationsUrl}"><spring:message code="admin.locales.action.translations"/></a>
                            <c:if test="${not locale.defaultLocale}">
                                <c:url var="toggleUrl" value="/admin/locales/${locale.id}/toggle"/>
                                <form class="inline-form" action="${toggleUrl}" method="post">
                                    <sec:csrfInput/>
                                    <button class="text-button" type="submit"><c:choose><c:when test="${locale.enabled}"><spring:message code="admin.locales.action.disable"/></c:when><c:otherwise><spring:message code="admin.locales.action.enable"/></c:otherwise></c:choose></button>
                                </form>
                                <c:url var="defaultUrl" value="/admin/locales/${locale.id}/default"/>
                                <form class="inline-form" action="${defaultUrl}" method="post">
                                    <sec:csrfInput/>
                                    <button class="text-button" type="submit"><spring:message code="admin.locales.action.makeDefault"/></button>
                                </form>
                            </c:if>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty locales}">
                <tr><td colspan="4"><spring:message code="admin.locales.empty"/></td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <spring:message var="localeEditorLabel" code="admin.locales.editor.label"/>
    <aside class="editor-pane" aria-label="<c:out value="${localeEditorLabel}"/>">
        <div class="editor-pane-header">
            <h2><c:choose><c:when test="${empty editingLocaleId}"><spring:message code="admin.locales.editor.create"/></c:when><c:otherwise><spring:message code="admin.locales.editor.update"/></c:otherwise></c:choose></h2>
            <p><spring:message code="admin.locales.editor.help"/></p>
        </div>
        <c:choose>
            <c:when test="${empty editingLocaleId}"><c:url var="localeFormAction" value="/admin/locales"/></c:when>
            <c:otherwise><c:url var="localeFormAction" value="/admin/locales/${editingLocaleId}"/></c:otherwise>
        </c:choose>
        <form:form method="post" action="${localeFormAction}" modelAttribute="localeForm">
            <form:errors path="*" cssClass="notice notice-error" element="div"/>
            <div class="field-grid">
                <div class="field">
                    <form:label path="languageTag"><spring:message code="admin.locales.field.languageTag"/></form:label>
                    <form:input path="languageTag" maxlength="35" placeholder="fr-CA" autocomplete="off"/>
                    <form:errors path="languageTag" cssClass="field-error"/>
                </div>
                <div class="field">
                    <form:label path="displayOrder"><spring:message code="admin.locales.field.order"/></form:label>
                    <form:input path="displayOrder" type="number" min="0" max="9999"/>
                    <form:errors path="displayOrder" cssClass="field-error"/>
                </div>
            </div>
            <div class="field">
                <form:label path="displayName"><spring:message code="admin.locales.field.displayName"/></form:label>
                <form:input path="displayName" maxlength="80" autocomplete="off"/>
                <form:errors path="displayName" cssClass="field-error"/>
            </div>
            <div class="field">
                <form:label path="nativeName"><spring:message code="admin.locales.field.nativeName"/></form:label>
                <form:input path="nativeName" maxlength="80" autocomplete="off"/>
                <form:errors path="nativeName" cssClass="field-error"/>
            </div>
            <div class="check-field">
                <form:checkbox path="enabled"/>
                <form:label path="enabled"><spring:message code="admin.locales.field.enabled"/></form:label>
            </div>
            <div class="form-actions">
                <button class="button" type="submit"><c:choose><c:when test="${empty editingLocaleId}"><spring:message code="admin.locales.submit.create"/></c:when><c:otherwise><spring:message code="admin.locales.submit.update"/></c:otherwise></c:choose></button>
            </div>
        </form:form>
    </aside>
</div>

<%@ include file="../fragments/footer.jspf" %>
