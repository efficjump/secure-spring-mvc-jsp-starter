<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="접근 거부"/>
<%@ include file="../fragments/header.jspf" %>
<section class="error-state">
    <p class="error-code">403</p>
    <h1>이 페이지에 접근할 권한이 없습니다.</h1>
    <p>필요한 권한이 있는 계정인지 확인해 주세요.</p>
    <c:if test="${not empty requestId}"><p class="request-id">요청 ID: <c:out value="${requestId}"/></p></c:if>
</section>
<%@ include file="../fragments/footer.jspf" %>

