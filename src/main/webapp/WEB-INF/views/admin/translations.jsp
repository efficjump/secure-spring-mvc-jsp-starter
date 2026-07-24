<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="admin.translations.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>
<c:url var="localizationGridScriptUrl" value="/assets/js/localization-grid.js"/>
<script src="${localizationGridScriptUrl}" defer></script>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="admin.translations.kicker"/></p>
        <h1><spring:message code="admin.translations.heading"/></h1>
        <p><spring:message code="admin.translations.description"/></p>
    </div>
    <div class="toolbar-actions">
        <span class="count-badge"><spring:message code="admin.translations.count" arguments="${messages.totalElements}"/></span>
        <c:url var="localesUrl" value="/admin/locales"/>
        <a class="button button-quiet" href="${localesUrl}"><spring:message code="admin.translations.languages"/></a>
    </div>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><spring:message code="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><spring:message code="${error}"/></div></c:if>
<form:form modelAttribute="translationRowForm" cssClass="validation-summary-form">
    <form:errors path="*" cssClass="notice notice-error" element="div"/>
</form:form>

<c:url var="searchUrl" value="/admin/locales/messages"/>
<spring:message var="translationSearchLabel" code="admin.translations.search.label"/>
<spring:message var="translationSearchPlaceholder" code="admin.translations.search.placeholder"/>
<form class="filter-bar filter-bar-single" method="get" action="${searchUrl}">
    <div>
        <label class="visually-hidden" for="translation-search"><c:out value="${translationSearchLabel}"/></label>
        <input id="translation-search" name="search" type="search" value="<c:out value='${search}'/>"
               placeholder="<c:out value='${translationSearchPlaceholder}'/>" maxlength="128">
    </div>
    <button class="button" type="submit"><spring:message code="admin.translations.search.submit"/></button>
</form>

<spring:message var="dirtySummaryLabel" code="common.unsavedCount"/>
<div class="editable-grid-shell translation-matrix-shell" data-editable-grid data-dirty-label="<c:out value='${dirtySummaryLabel}'/>">
    <div class="editable-grid-toolbar">
        <div>
            <strong><spring:message code="admin.translations.grid.title"/></strong>
            <p><spring:message code="admin.translations.grid.help"/></p>
        </div>
        <span class="dirty-summary" data-dirty-summary aria-live="polite"></span>
    </div>
    <div class="table-wrap editable-grid-wrap translation-grid-wrap">
        <table class="editable-grid translation-grid">
            <thead>
            <tr>
                <th id="message-key-column" class="sticky-key-column" scope="col">
                    <spring:message code="admin.translations.column.key"/>
                </th>
                <c:forEach var="locale" items="${locales}">
                    <th id="locale-column-${locale.id}" class="translation-language-column" scope="col">
                        <span class="translation-language-name"><c:out value="${locale.nativeName}"/></span>
                        <span class="translation-language-meta">
                            <span class="mono"><c:out value="${locale.languageTag}"/></span>
                            <c:if test="${not locale.enabled}">
                                <span class="status status-muted"><spring:message code="common.status.inactive"/></span>
                            </c:if>
                            <c:if test="${locale.defaultLocale}">
                                <span class="status status-info"><spring:message code="admin.locales.default"/></span>
                            </c:if>
                        </span>
                    </th>
                </c:forEach>
                <th class="sticky-action-column" scope="col"><spring:message code="admin.translations.column.actions"/></th>
            </tr>
            </thead>
            <tbody>
            <c:set var="newRowFormId" value="translation-row-new"/>
            <tr class="grid-new-row translation-new-row" data-editable-row>
                <th class="sticky-key-column" scope="row">
                    <c:url var="saveRowUrl" value="/admin/locales/messages"/>
                    <form id="${newRowFormId}" class="detached-form" action="${saveRowUrl}" method="post">
                        <sec:csrfInput/>
                        <input type="hidden" name="page" value="<c:out value='${messages.number}'/>">
                        <input type="hidden" name="search" value="<c:out value='${search}'/>">
                    </form>
                    <label class="visually-hidden" for="new-message-key"><spring:message code="admin.translations.field.key"/></label>
                    <input id="new-message-key" class="grid-input mono" type="text" name="messageKey"
                           maxlength="190" required autocomplete="off" placeholder="page.section.label"
                           form="${newRowFormId}" value="<c:out value='${failedRowOnPage ? "" : translationRowForm.messageKey}'/>"
                           data-original-value="">
                    <span class="grid-key-help"><spring:message code="admin.translations.new.help"/></span>
                </th>
                <c:forEach var="cell" items="${newTranslationCells}">
                    <c:set var="newCellValue" value=""/>
                    <c:if test="${not failedRowOnPage}">
                        <c:set var="newCellValue" value="${translationRowForm.values[cell.localeId]}"/>
                    </c:if>
                    <td class="translation-cell">
                        <label class="visually-hidden" for="new-message-${cell.localeId}">
                            <spring:message code="admin.translations.new.cell.label" arguments="${cell.nativeName}"/>
                        </label>
                        <textarea id="new-message-${cell.localeId}" class="grid-textarea" rows="3" maxlength="4000"
                                  name="values[${cell.localeId}]" form="${newRowFormId}"
                                  data-original-value=""><c:out value="${newCellValue}"/></textarea>
                        <span class="translation-source status status-muted"><spring:message code="common.status.new"/></span>
                    </td>
                </c:forEach>
                <td class="sticky-action-column">
                    <div class="grid-row-actions">
                        <button class="button button-compact" type="submit" form="${newRowFormId}">
                            <spring:message code="admin.translations.add"/>
                        </button>
                        <span class="dirty-indicator" data-dirty-indicator hidden><spring:message code="common.unsaved"/></span>
                    </div>
                </td>
            </tr>

            <c:forEach var="entry" items="${messages.content}" varStatus="rowStatus">
                <c:set var="rowFormId" value="translation-row-${rowStatus.index}"/>
                <c:set var="failedRow" value="${failedMessageKey eq entry.messageKey}"/>
                <tr data-editable-row <c:if test="${failedRow}">class="grid-row-invalid"</c:if>>
                    <th id="message-key-${rowStatus.index}" class="sticky-key-column message-key-cell" scope="row">
                        <form id="${rowFormId}" class="detached-form" action="${saveRowUrl}" method="post">
                            <sec:csrfInput/>
                            <input type="hidden" name="messageKey" value="<c:out value='${entry.messageKey}'/>">
                            <input type="hidden" name="page" value="<c:out value='${messages.number}'/>">
                            <input type="hidden" name="search" value="<c:out value='${search}'/>">
                        </form>
                        <span class="mono"><c:out value="${entry.messageKey}"/></span>
                    </th>
                    <c:forEach var="cell" items="${entry.cells}">
                        <c:set var="cellValue" value="${cell.value}"/>
                        <c:if test="${failedRow}">
                            <c:set var="cellValue" value="${translationRowForm.values[cell.localeId]}"/>
                        </c:if>
                        <td class="translation-cell">
                            <textarea id="message-${rowStatus.index}-${cell.localeId}" class="grid-textarea" rows="3"
                                      maxlength="4000" name="values[${cell.localeId}]" form="${rowFormId}"
                                      aria-labelledby="message-key-${rowStatus.index} locale-column-${cell.localeId}"
                                      data-original-value="<c:out value='${cell.value}'/>"><c:out value="${cellValue}"/></textarea>
                            <c:choose>
                                <c:when test="${cell.overridden}">
                                    <span class="translation-source status status-info"><spring:message code="admin.translations.source.database"/></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="translation-source status status-muted"><spring:message code="admin.translations.source.bundle"/></span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </c:forEach>
                    <td class="sticky-action-column">
                        <div class="grid-row-actions">
                            <button class="button button-compact" type="submit" form="${rowFormId}">
                                <spring:message code="common.save"/>
                            </button>
                            <span class="dirty-indicator" data-dirty-indicator hidden><spring:message code="common.unsaved"/></span>
                            <span class="keyboard-hint"><spring:message code="admin.translations.keyboardHint"/></span>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty messages.content}">
                <tr><td colspan="${locales.size() + 2}"><spring:message code="admin.translations.empty"/></td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<spring:message var="translationsPaginationLabel" code="admin.translations.pagination"/>
<c:set var="translationsPageNumber" value="${messages.number + 1}"/>
<c:set var="translationsTotalPages" value="${messages.totalPages == 0 ? 1 : messages.totalPages}"/>
<nav class="pagination" aria-label="<c:out value='${translationsPaginationLabel}'/>">
    <span><spring:message code="common.pageIndicator" arguments="${translationsPageNumber},${translationsTotalPages}"/></span>
    <div class="pagination-links">
        <c:if test="${messages.hasPrevious()}">
            <c:url var="previousUrl" value="/admin/locales/messages">
                <c:param name="page" value="${messages.number - 1}"/>
                <c:param name="search" value="${search}"/>
            </c:url>
            <a class="button button-quiet" href="${previousUrl}"><spring:message code="common.previous"/></a>
        </c:if>
        <c:if test="${messages.hasNext()}">
            <c:url var="nextUrl" value="/admin/locales/messages">
                <c:param name="page" value="${messages.number + 1}"/>
                <c:param name="search" value="${search}"/>
            </c:url>
            <a class="button button-quiet" href="${nextUrl}"><spring:message code="common.next"/></a>
        </c:if>
    </div>
</nav>

<p class="page-help"><spring:message code="admin.translations.field.value.help"/></p>

<%@ include file="../fragments/footer.jspf" %>
