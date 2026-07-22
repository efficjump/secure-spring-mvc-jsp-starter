<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="페이지 없음"/>
<%@ include file="../fragments/header.jspf" %>
<section class="error-state">
    <p class="error-code">404</p>
    <h1>요청한 페이지를 찾을 수 없습니다.</h1>
    <p>주소를 확인하거나 홈에서 다시 시작해 주세요.</p>
    <c:if test="${not empty requestId}"><p class="request-id">요청 ID: <c:out value="${requestId}"/></p></c:if>
</section>
<%@ include file="../fragments/footer.jspf" %>

