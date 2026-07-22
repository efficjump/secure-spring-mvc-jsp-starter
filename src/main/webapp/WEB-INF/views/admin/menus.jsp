<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="pageTitle" value="메뉴 관리"/>
<%@ include file="../fragments/header.jspf" %>

<section class="page-toolbar">
    <div>
        <p class="section-kicker">Workspace configuration</p>
        <h1>메뉴 관리</h1>
        <p>업무 셸의 메뉴 그룹, 표시 순서, 접근 권한과 내부 경로를 데이터 기반으로 구성합니다.</p>
    </div>
    <c:url var="newMenuUrl" value="/admin/menus"/>
    <a class="button button-quiet" href="${newMenuUrl}">새 메뉴</a>
</section>

<c:if test="${not empty message}"><div class="notice notice-success"><c:out value="${message}"/></div></c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><c:out value="${error}"/></div></c:if>

<div class="editor-layout">
    <div class="table-wrap">
        <table>
            <thead>
            <tr>
                <th scope="col">순서</th>
                <th scope="col">메뉴</th>
                <th scope="col">그룹 / 경로</th>
                <th scope="col">권한</th>
                <th scope="col">상태</th>
                <th scope="col">관리</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="menu" items="${menus}">
                <tr <c:if test="${editingMenuId eq menu.id}">class="selected-row"</c:if>>
                    <td class="mono"><c:out value="${menu.displayOrder}"/></td>
                    <td>
                        <strong><c:out value="${menu.label}"/></strong>
                        <span class="secondary mono"><c:out value="${menu.menuKey}"/> · <c:out value="${menu.icon.label}"/></span>
                    </td>
                    <td>
                        <strong><c:out value="${menu.menuGroup}"/></strong>
                        <span class="secondary mono"><c:out value="${menu.path}"/></span>
                    </td>
                    <td><c:choose><c:when test="${menu.requiredRole eq 'ADMIN'}">관리자</c:when><c:otherwise>사용자</c:otherwise></c:choose></td>
                    <td><c:choose><c:when test="${menu.enabled}"><span class="status status-success">표시</span></c:when><c:otherwise><span class="status status-muted">숨김</span></c:otherwise></c:choose></td>
                    <td>
                        <div class="action-stack">
                            <c:url var="editUrl" value="/admin/menus"><c:param name="edit" value="${menu.id}"/></c:url>
                            <a class="text-button" href="${editUrl}">편집</a>
                            <c:url var="toggleUrl" value="/admin/menus/${menu.id}/toggle"/>
                            <form class="inline-form" action="${toggleUrl}" method="post">
                                <sec:csrfInput/>
                                <button class="text-button" type="submit"><c:choose><c:when test="${menu.enabled}">숨기기</c:when><c:otherwise>표시하기</c:otherwise></c:choose></button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <aside class="editor-pane" aria-label="메뉴 편집">
        <div class="editor-pane-header">
            <h2><c:choose><c:when test="${empty editingMenuId}">새 메뉴 추가</c:when><c:otherwise>메뉴 설정 편집</c:otherwise></c:choose></h2>
            <p>외부 URL과 쿼리 문자열은 보안상 허용하지 않습니다.</p>
        </div>
        <c:choose>
            <c:when test="${empty editingMenuId}"><c:url var="menuFormAction" value="/admin/menus"/></c:when>
            <c:otherwise><c:url var="menuFormAction" value="/admin/menus/${editingMenuId}"/></c:otherwise>
        </c:choose>
        <form:form method="post" action="${menuFormAction}" modelAttribute="menuForm">
            <form:errors path="*" cssClass="notice notice-error" element="div"/>
            <div class="field-grid">
                <div class="field">
                    <form:label path="menuKey">메뉴 키</form:label>
                    <form:input path="menuKey" maxlength="40" placeholder="system-report"/>
                    <form:errors path="menuKey" cssClass="field-error"/>
                </div>
                <div class="field">
                    <form:label path="displayOrder">표시 순서</form:label>
                    <form:input path="displayOrder" type="number" min="0" max="9999"/>
                    <form:errors path="displayOrder" cssClass="field-error"/>
                </div>
            </div>
            <div class="field">
                <form:label path="label">표시 이름</form:label>
                <form:input path="label" maxlength="80"/>
                <form:errors path="label" cssClass="field-error"/>
            </div>
            <div class="field">
                <form:label path="menuGroup">메뉴 그룹</form:label>
                <form:input path="menuGroup" maxlength="60"/>
                <form:errors path="menuGroup" cssClass="field-error"/>
            </div>
            <div class="field">
                <form:label path="path">내부 경로</form:label>
                <form:input path="path" maxlength="180" placeholder="/reports/overview"/>
                <form:errors path="path" cssClass="field-error"/>
            </div>
            <div class="field-grid">
                <div class="field">
                    <form:label path="icon">아이콘</form:label>
                    <form:select path="icon">
                        <form:options items="${iconOptions}" itemLabel="label"/>
                    </form:select>
                    <form:errors path="icon" cssClass="field-error"/>
                </div>
                <div class="field">
                    <form:label path="requiredRole">필요 권한</form:label>
                    <form:select path="requiredRole">
                        <form:option value="USER">사용자</form:option>
                        <form:option value="ADMIN">관리자</form:option>
                    </form:select>
                    <form:errors path="requiredRole" cssClass="field-error"/>
                </div>
            </div>
            <div class="check-field">
                <form:checkbox path="enabled"/>
                <form:label path="enabled">업무 메뉴에 표시</form:label>
            </div>
            <div class="form-actions">
                <button class="button" type="submit"><c:choose><c:when test="${empty editingMenuId}">메뉴 추가</c:when><c:otherwise>설정 저장</c:otherwise></c:choose></button>
            </div>
        </form:form>
    </aside>
</div>

<%@ include file="../fragments/footer.jspf" %>
