<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="admin.translations.title" arguments="${locale.nativeName}"/></c:set>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="admin.translations.kicker"/></p>
        <h1><spring:message code="admin.translations.heading" arguments="${locale.nativeName}"/></h1>
        <p><spring:message code="admin.translations.description" arguments="${locale.languageTag}"/></p>
    </div>
    <div class="toolbar-actions">
        <span class="count-badge"><spring:message code="admin.translations.count" arguments="${messages.totalElements}"/></span>
        <c:url var="localesUrl" value="/admin/locales"/>
        <a class="button button-quiet" href="${localesUrl}"><spring:message code="admin.translations.back"/></a>
    </div>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><spring:message code="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><spring:message code="${error}"/></div></c:if>

<c:url var="searchUrl" value="/admin/locales/${locale.id}/messages"/>
<spring:message var="translationSearchLabel" code="admin.translations.search.label"/>
<spring:message var="translationSearchPlaceholder" code="admin.translations.search.placeholder"/>
<form class="filter-bar filter-bar-single" method="get" action="${searchUrl}">
    <div>
        <label class="visually-hidden" for="translation-search"><c:out value="${translationSearchLabel}"/></label>
        <input id="translation-search" name="search" type="search" value="<c:out value="${search}"/>" placeholder="<c:out value="${translationSearchPlaceholder}"/>" maxlength="128">
    </div>
    <button class="button" type="submit"><spring:message code="admin.translations.search.submit"/></button>
</form>

<div class="editor-layout translation-editor-layout">
    <div>
        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th scope="col"><spring:message code="admin.translations.column.key"/></th>
                    <th scope="col"><spring:message code="admin.translations.column.value"/></th>
                    <th scope="col"><spring:message code="admin.translations.column.source"/></th>
                    <th scope="col"><spring:message code="admin.translations.column.actions"/></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="entry" items="${messages.content}">
                    <tr <c:if test="${editingMessageKey eq entry.messageKey}">class="selected-row"</c:if>>
                        <td class="mono message-key-cell"><c:out value="${entry.messageKey}"/></td>
                        <td><span class="message-preview"><c:out value="${entry.resolvedValue}"/></span></td>
                        <td><c:choose><c:when test="${entry.overridden}"><span class="status status-info"><spring:message code="admin.translations.source.database"/></span></c:when><c:otherwise><span class="status status-muted"><spring:message code="admin.translations.source.bundle"/></span></c:otherwise></c:choose></td>
                        <td>
                            <div class="action-stack">
                                <c:url var="editUrl" value="/admin/locales/${locale.id}/messages">
                                    <c:param name="edit" value="${entry.messageKey}"/>
                                    <c:if test="${not empty search}"><c:param name="search" value="${search}"/></c:if>
                                    <c:param name="page" value="${messages.number}"/>
                                </c:url>
                                <a class="text-button" href="${editUrl}"><spring:message code="admin.translations.action.edit"/></a>
                                <c:if test="${entry.overridden}">
                                    <c:url var="deleteUrl" value="/admin/locales/${locale.id}/messages/delete"/>
                                    <form class="inline-form" action="${deleteUrl}" method="post">
                                        <sec:csrfInput/>
                                        <input type="hidden" name="messageKey" value="<c:out value="${entry.messageKey}"/>">
                                        <input type="hidden" name="search" value="<c:out value="${search}"/>">
                                        <button class="text-button text-button-danger" type="submit"><spring:message code="admin.translations.action.restore"/></button>
                                    </form>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty messages.content}">
                    <tr><td colspan="4"><spring:message code="admin.translations.empty"/></td></tr>
                </c:if>
                </tbody>
            </table>
        </div>

        <spring:message var="translationsPaginationLabel" code="admin.translations.pagination"/>
        <c:set var="translationsPageNumber" value="${messages.number + 1}"/>
        <c:set var="translationsTotalPages" value="${messages.totalPages == 0 ? 1 : messages.totalPages}"/>
        <nav class="pagination" aria-label="<c:out value="${translationsPaginationLabel}"/>">
            <span><spring:message code="common.pageIndicator" arguments="${translationsPageNumber},${translationsTotalPages}"/></span>
            <div class="pagination-links">
                <c:if test="${messages.hasPrevious()}">
                    <c:url var="previousUrl" value="/admin/locales/${locale.id}/messages">
                        <c:param name="page" value="${messages.number - 1}"/>
                        <c:param name="search" value="${search}"/>
                    </c:url>
                    <a class="button button-quiet" href="${previousUrl}"><spring:message code="common.previous"/></a>
                </c:if>
                <c:if test="${messages.hasNext()}">
                    <c:url var="nextUrl" value="/admin/locales/${locale.id}/messages">
                        <c:param name="page" value="${messages.number + 1}"/>
                        <c:param name="search" value="${search}"/>
                    </c:url>
                    <a class="button button-quiet" href="${nextUrl}"><spring:message code="common.next"/></a>
                </c:if>
            </div>
        </nav>
    </div>

    <spring:message var="translationEditorLabel" code="admin.translations.editor.label"/>
    <aside class="editor-pane" aria-label="<c:out value="${translationEditorLabel}"/>">
        <div class="editor-pane-header">
            <h2><c:choose><c:when test="${empty editingMessageKey}"><spring:message code="admin.translations.editor.create"/></c:when><c:otherwise><spring:message code="admin.translations.editor.update"/></c:otherwise></c:choose></h2>
            <p><spring:message code="admin.translations.editor.help"/></p>
        </div>
        <c:url var="messageFormAction" value="/admin/locales/${locale.id}/messages">
            <c:param name="page" value="${messages.number}"/>
            <c:if test="${not empty search}"><c:param name="search" value="${search}"/></c:if>
        </c:url>
        <form:form method="post" action="${messageFormAction}" modelAttribute="messageForm">
            <form:errors path="*" cssClass="notice notice-error" element="div"/>
            <div class="field">
                <form:label path="messageKey"><spring:message code="admin.translations.field.key"/></form:label>
                <form:input path="messageKey" maxlength="190" autocomplete="off" placeholder="page.section.label"/>
                <form:errors path="messageKey" cssClass="field-error"/>
            </div>
            <div class="field">
                <form:label path="messageValue"><spring:message code="admin.translations.field.value"/></form:label>
                <form:textarea path="messageValue" rows="7" maxlength="4000"/>
                <form:errors path="messageValue" cssClass="field-error"/>
            </div>
            <p class="help-text"><spring:message code="admin.translations.field.value.help"/></p>
            <div class="form-actions">
                <button class="button" type="submit"><spring:message code="admin.translations.submit"/></button>
            </div>
        </form:form>
    </aside>
</div>

<%@ include file="../fragments/footer.jspf" %>
