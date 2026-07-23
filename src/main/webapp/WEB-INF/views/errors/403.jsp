<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="error.403.title"/></c:set>
<%@ include file="../fragments/header.jspf" %>
<section class="error-state">
    <p class="error-code">403</p>
    <h1><spring:message code="error.403.heading"/></h1>
    <p><spring:message code="error.403.description"/></p>
    <c:if test="${not empty requestId}"><p class="request-id"><spring:message code="common.requestId" arguments="${requestId}"/></p></c:if>
</section>
<%@ include file="../fragments/footer.jspf" %>
