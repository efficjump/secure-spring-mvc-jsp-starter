<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="요청 처리 오류"/>
<%@ include file="../fragments/header.jspf" %>
<section class="error-state">
    <p class="error-code"><c:out value="${status}"/></p>
    <h1>요청을 안전하게 처리하지 못했습니다.</h1>
    <p>잠시 후 다시 시도해 주세요. 문제가 계속되면 요청 ID와 함께 운영자에게 알려 주세요.</p>
    <c:if test="${not empty requestId}"><p class="request-id">요청 ID: <c:out value="${requestId}"/></p></c:if>
</section>
<%@ include file="../fragments/footer.jspf" %>
