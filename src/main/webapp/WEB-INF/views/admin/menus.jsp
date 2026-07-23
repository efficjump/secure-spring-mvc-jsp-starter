<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="admin.menus.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker"><spring:message code="admin.menus.kicker"/></p>
        <h1><spring:message code="admin.menus.heading"/></h1>
        <p><spring:message code="admin.menus.description"/></p>
    </div>
    <c:url var="newMenuUrl" value="/admin/menus"/>
    <a class="button button-quiet" href="${newMenuUrl}"><spring:message code="admin.menus.new"/></a>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><spring:message code="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><spring:message code="${error}"/></div></c:if>

<div class="editor-layout">
    <div class="table-wrap">
        <table>
            <thead>
            <tr>
                <th scope="col"><spring:message code="admin.menus.column.order"/></th>
                <th scope="col"><spring:message code="admin.menus.column.menu"/></th>
                <th scope="col"><spring:message code="admin.menus.column.groupPath"/></th>
                <th scope="col"><spring:message code="admin.menus.column.role"/></th>
                <th scope="col"><spring:message code="admin.menus.column.status"/></th>
                <th scope="col"><spring:message code="admin.menus.column.actions"/></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="menu" items="${menus}">
                <tr <c:if test="${editingMenuId eq menu.id}">class="selected-row"</c:if>>
                    <td class="mono"><c:out value="${menu.displayOrder}"/></td>
                    <td>
                        <strong><c:out value="${menu.label}"/></strong>
                        <span class="secondary mono"><c:out value="${menu.menuKey}"/> · <spring:message code="${menu.icon.messageCode}"/></span>
                    </td>
                    <td>
                        <strong><c:out value="${menu.menuGroup}"/></strong>
                        <span class="secondary mono"><c:out value="${menu.path}"/></span>
                    </td>
                    <td><spring:message code="${menu.requiredRole.messageCode}"/></td>
                    <td><c:choose><c:when test="${menu.enabled}"><span class="status status-success"><spring:message code="common.status.visible"/></span></c:when><c:otherwise><span class="status status-muted"><spring:message code="common.status.hidden"/></span></c:otherwise></c:choose></td>
                    <td>
                        <div class="action-stack">
                            <c:url var="editUrl" value="/admin/menus"><c:param name="edit" value="${menu.id}"/></c:url>
                            <a class="text-button" href="${editUrl}"><spring:message code="admin.menus.action.edit"/></a>
                            <c:url var="toggleUrl" value="/admin/menus/${menu.id}/toggle"/>
                            <form class="inline-form" action="${toggleUrl}" method="post">
                                <sec:csrfInput/>
                                <button class="text-button" type="submit"><c:choose><c:when test="${menu.enabled}"><spring:message code="admin.menus.action.hide"/></c:when><c:otherwise><spring:message code="admin.menus.action.show"/></c:otherwise></c:choose></button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <spring:message var="menuEditorLabel" code="admin.menus.editor.label"/>
    <aside class="editor-pane" aria-label="<c:out value="${menuEditorLabel}"/>">
        <div class="editor-pane-header">
            <h2><c:choose><c:when test="${empty editingMenuId}"><spring:message code="admin.menus.editor.create"/></c:when><c:otherwise><spring:message code="admin.menus.editor.update"/></c:otherwise></c:choose></h2>
            <p><spring:message code="admin.menus.editor.help"/></p>
        </div>
        <c:choose>
            <c:when test="${empty editingMenuId}"><c:url var="menuFormAction" value="/admin/menus"/></c:when>
            <c:otherwise><c:url var="menuFormAction" value="/admin/menus/${editingMenuId}"/></c:otherwise>
        </c:choose>
        <form:form method="post" action="${menuFormAction}" modelAttribute="menuForm">
            <form:errors path="*" cssClass="notice notice-error" element="div"/>
            <div class="field-grid">
                <div class="field">
                    <form:label path="menuKey"><spring:message code="admin.menus.field.key"/></form:label>
                    <form:input path="menuKey" maxlength="40" placeholder="system-report"/>
                    <form:errors path="menuKey" cssClass="field-error"/>
                </div>
                <div class="field">
                    <form:label path="displayOrder"><spring:message code="admin.menus.field.order"/></form:label>
                    <form:input path="displayOrder" type="number" min="0" max="9999"/>
                    <form:errors path="displayOrder" cssClass="field-error"/>
                </div>
            </div>
            <div class="field">
                <form:label path="label"><spring:message code="admin.menus.field.label"/></form:label>
                <form:input path="label" maxlength="80"/>
                <form:errors path="label" cssClass="field-error"/>
            </div>
            <div class="field">
                <form:label path="menuGroup"><spring:message code="admin.menus.field.group"/></form:label>
                <form:input path="menuGroup" maxlength="60"/>
                <form:errors path="menuGroup" cssClass="field-error"/>
            </div>
            <div class="field">
                <form:label path="path"><spring:message code="admin.menus.field.path"/></form:label>
                <form:input path="path" maxlength="180" placeholder="/reports/overview"/>
                <form:errors path="path" cssClass="field-error"/>
            </div>
            <div class="field-grid">
                <div class="field">
                    <form:label path="icon"><spring:message code="admin.menus.field.icon"/></form:label>
                    <form:select path="icon">
                        <c:forEach var="iconOption" items="${iconOptions}">
                            <form:option value="${iconOption}"><spring:message code="${iconOption.messageCode}"/></form:option>
                        </c:forEach>
                    </form:select>
                    <form:errors path="icon" cssClass="field-error"/>
                </div>
                <div class="field">
                    <form:label path="requiredRole"><spring:message code="admin.menus.field.role"/></form:label>
                    <form:select path="requiredRole">
                        <c:forEach var="roleOption" items="${roleOptions}">
                            <form:option value="${roleOption}"><spring:message code="${roleOption.messageCode}"/></form:option>
                        </c:forEach>
                    </form:select>
                    <form:errors path="requiredRole" cssClass="field-error"/>
                </div>
            </div>
            <div class="check-field">
                <form:checkbox path="enabled"/>
                <form:label path="enabled"><spring:message code="admin.menus.field.enabled"/></form:label>
            </div>
            <div class="form-actions">
                <button class="button" type="submit"><c:choose><c:when test="${empty editingMenuId}"><spring:message code="admin.menus.submit.create"/></c:when><c:otherwise><spring:message code="admin.menus.submit.update"/></c:otherwise></c:choose></button>
            </div>
        </form:form>
    </aside>
</div>

<%@ include file="../fragments/footer.jspf" %>
