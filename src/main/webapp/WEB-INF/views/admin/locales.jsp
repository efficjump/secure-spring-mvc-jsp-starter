<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="admin.locales.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>
<c:url var="localizationGridScriptUrl" value="/assets/js/localization-grid.js"/>
<script src="${localizationGridScriptUrl}" defer></script>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="admin.locales.kicker"/></p>
        <h1><spring:message code="admin.locales.heading"/></h1>
        <p><spring:message code="admin.locales.description"/></p>
    </div>
    <div class="toolbar-actions">
        <c:url var="translationsUrl" value="/admin/locales/messages"/>
        <a class="button" href="${translationsUrl}"><spring:message code="admin.locales.action.translations"/></a>
        <a class="button button-quiet" href="#new-locale-row"><spring:message code="admin.locales.new"/></a>
    </div>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><spring:message code="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><spring:message code="${error}"/></div></c:if>
<form:form modelAttribute="localeForm" cssClass="validation-summary-form">
    <form:errors path="*" cssClass="notice notice-error" element="div"/>
</form:form>

<spring:message var="dirtySummaryLabel" code="common.unsavedCount"/>
<div class="editable-grid-shell" data-editable-grid data-dirty-label="<c:out value='${dirtySummaryLabel}'/>">
    <div class="editable-grid-toolbar">
        <p><spring:message code="admin.locales.grid.help"/></p>
        <span class="dirty-summary" data-dirty-summary aria-live="polite"></span>
    </div>
    <div class="table-wrap editable-grid-wrap">
        <table class="editable-grid locale-grid">
            <thead>
            <tr>
                <th scope="col"><spring:message code="admin.locales.column.order"/></th>
                <th scope="col"><spring:message code="admin.locales.field.languageTag"/></th>
                <th scope="col"><spring:message code="admin.locales.field.displayName"/></th>
                <th scope="col"><spring:message code="admin.locales.field.nativeName"/></th>
                <th scope="col"><spring:message code="admin.locales.column.status"/></th>
                <th scope="col"><spring:message code="admin.locales.column.actions"/></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="locale" items="${locales}" varStatus="rowStatus">
                <c:set var="rowFormId" value="locale-update-${locale.id}"/>
                <c:choose>
                    <c:when test="${editingLocaleId eq locale.id}">
                        <c:set var="rowOrder" value="${localeForm.displayOrder}"/>
                        <c:set var="rowLanguageTag" value="${localeForm.languageTag}"/>
                        <c:set var="rowDisplayName" value="${localeForm.displayName}"/>
                        <c:set var="rowNativeName" value="${localeForm.nativeName}"/>
                        <c:set var="rowEnabled" value="${localeForm.enabled}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="rowOrder" value="${locale.displayOrder}"/>
                        <c:set var="rowLanguageTag" value="${locale.languageTag}"/>
                        <c:set var="rowDisplayName" value="${locale.displayName}"/>
                        <c:set var="rowNativeName" value="${locale.nativeName}"/>
                        <c:set var="rowEnabled" value="${locale.enabled}"/>
                    </c:otherwise>
                </c:choose>
                <tr data-editable-row <c:if test="${editingLocaleId eq locale.id}">class="grid-row-invalid"</c:if>>
                    <td class="grid-order-cell">
                        <c:url var="updateLocaleUrl" value="/admin/locales/${locale.id}"/>
                        <form id="${rowFormId}" class="detached-form" action="${updateLocaleUrl}" method="post">
                            <sec:csrfInput/>
                        </form>
                        <spring:message var="orderLabel" code="admin.locales.grid.order.label" arguments="${locale.nativeName}"/>
                        <label class="visually-hidden" for="locale-order-${locale.id}"><c:out value="${orderLabel}"/></label>
                        <input id="locale-order-${locale.id}" class="grid-input grid-input-number" type="number"
                               name="displayOrder" min="0" max="9999" required form="${rowFormId}"
                               value="<c:out value='${rowOrder}'/>" data-original-value="<c:out value='${locale.displayOrder}'/>">
                    </td>
                    <td>
                        <spring:message var="tagLabel" code="admin.locales.grid.tag.label" arguments="${locale.nativeName}"/>
                        <label class="visually-hidden" for="locale-tag-${locale.id}"><c:out value="${tagLabel}"/></label>
                        <input id="locale-tag-${locale.id}" class="grid-input mono" type="text"
                               name="languageTag" maxlength="35" required autocomplete="off" form="${rowFormId}"
                               value="<c:out value='${rowLanguageTag}'/>" data-original-value="<c:out value='${locale.languageTag}'/>">
                    </td>
                    <td>
                        <spring:message var="displayNameLabel" code="admin.locales.grid.displayName.label" arguments="${locale.nativeName}"/>
                        <label class="visually-hidden" for="locale-display-name-${locale.id}"><c:out value="${displayNameLabel}"/></label>
                        <input id="locale-display-name-${locale.id}" class="grid-input" type="text"
                               name="displayName" maxlength="80" required autocomplete="off" form="${rowFormId}"
                               value="<c:out value='${rowDisplayName}'/>" data-original-value="<c:out value='${locale.displayName}'/>">
                    </td>
                    <td>
                        <spring:message var="nativeNameLabel" code="admin.locales.grid.nativeName.label" arguments="${locale.nativeName}"/>
                        <label class="visually-hidden" for="locale-native-name-${locale.id}"><c:out value="${nativeNameLabel}"/></label>
                        <input id="locale-native-name-${locale.id}" class="grid-input" type="text"
                               name="nativeName" maxlength="80" required autocomplete="off" form="${rowFormId}"
                               value="<c:out value='${rowNativeName}'/>" data-original-value="<c:out value='${locale.nativeName}'/>">
                    </td>
                    <td>
                        <div class="grid-status-control">
                            <c:choose>
                                <c:when test="${locale.defaultLocale}">
                                    <input type="hidden" name="enabled" value="true" form="${rowFormId}">
                                    <input id="locale-enabled-${locale.id}" class="grid-checkbox" type="checkbox"
                                           checked disabled aria-describedby="locale-default-${locale.id}">
                                </c:when>
                                <c:otherwise>
                                    <input id="locale-enabled-${locale.id}" class="grid-checkbox" type="checkbox"
                                           name="enabled" value="true" form="${rowFormId}"
                                           <c:if test="${rowEnabled}">checked</c:if>
                                           data-original-value="<c:out value='${locale.enabled}'/>">
                                </c:otherwise>
                            </c:choose>
                            <label for="locale-enabled-${locale.id}"><spring:message code="admin.locales.field.enabled.short"/></label>
                        </div>
                        <div class="status-list">
                            <c:if test="${locale.defaultLocale}">
                                <span id="locale-default-${locale.id}" class="status status-info"><spring:message code="admin.locales.default"/></span>
                            </c:if>
                            <span class="dirty-indicator" data-dirty-indicator hidden><spring:message code="common.unsaved"/></span>
                        </div>
                    </td>
                    <td>
                        <div class="grid-actions">
                            <button class="button button-compact" type="submit" form="${rowFormId}">
                                <spring:message code="common.save"/>
                            </button>
                            <c:if test="${not locale.defaultLocale}">
                                <c:url var="defaultUrl" value="/admin/locales/${locale.id}/default"/>
                                <form class="inline-form" action="${defaultUrl}" method="post">
                                    <sec:csrfInput/>
                                    <button class="button button-quiet button-compact" type="submit">
                                        <spring:message code="admin.locales.action.makeDefault"/>
                                    </button>
                                </form>
                            </c:if>
                        </div>
                    </td>
                </tr>
            </c:forEach>

            <c:set var="newLocaleFormId" value="locale-create"/>
            <tr id="new-locale-row" class="grid-new-row" data-editable-row>
                <td class="grid-order-cell">
                    <c:url var="createLocaleUrl" value="/admin/locales"/>
                    <form id="${newLocaleFormId}" class="detached-form" action="${createLocaleUrl}" method="post">
                        <sec:csrfInput/>
                    </form>
                    <label class="visually-hidden" for="new-locale-order"><spring:message code="admin.locales.field.order"/></label>
                    <input id="new-locale-order" class="grid-input grid-input-number" type="number"
                           name="displayOrder" min="0" max="9999" required form="${newLocaleFormId}"
                           value="<c:out value='${localeForm.displayOrder}'/>" data-original-value="0">
                </td>
                <td>
                    <label class="visually-hidden" for="new-locale-tag"><spring:message code="admin.locales.field.languageTag"/></label>
                    <input id="new-locale-tag" class="grid-input mono" type="text"
                           name="languageTag" maxlength="35" required autocomplete="off"
                           placeholder="fr-CA" form="${newLocaleFormId}"
                           value="<c:out value='${empty editingLocaleId ? localeForm.languageTag : ""}'/>" data-original-value="">
                </td>
                <td>
                    <label class="visually-hidden" for="new-locale-display-name"><spring:message code="admin.locales.field.displayName"/></label>
                    <input id="new-locale-display-name" class="grid-input" type="text"
                           name="displayName" maxlength="80" required autocomplete="off" form="${newLocaleFormId}"
                           value="<c:out value='${empty editingLocaleId ? localeForm.displayName : ""}'/>" data-original-value="">
                </td>
                <td>
                    <label class="visually-hidden" for="new-locale-native-name"><spring:message code="admin.locales.field.nativeName"/></label>
                    <input id="new-locale-native-name" class="grid-input" type="text"
                           name="nativeName" maxlength="80" required autocomplete="off" form="${newLocaleFormId}"
                           value="<c:out value='${empty editingLocaleId ? localeForm.nativeName : ""}'/>" data-original-value="">
                </td>
                <td>
                    <div class="grid-status-control">
                        <input id="new-locale-enabled" class="grid-checkbox" type="checkbox"
                               name="enabled" value="true" form="${newLocaleFormId}"
                               <c:if test="${empty editingLocaleId ? localeForm.enabled : true}">checked</c:if>
                               data-original-value="true">
                        <label for="new-locale-enabled"><spring:message code="admin.locales.field.enabled.short"/></label>
                    </div>
                    <span class="status status-muted"><spring:message code="common.status.new"/></span>
                    <span class="dirty-indicator" data-dirty-indicator hidden><spring:message code="common.unsaved"/></span>
                </td>
                <td>
                    <button class="button button-compact" type="submit" form="${newLocaleFormId}">
                        <spring:message code="admin.locales.submit.create"/>
                    </button>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

<p class="page-help"><spring:message code="admin.locales.editor.help"/></p>

<%@ include file="../fragments/footer.jspf" %>
