<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle"><spring:message code="home.title"/></c:set>
<%@ include file="fragments/header.jspf" %>

<section class="hero">
    <div>
        <h1><spring:message code="home.heading"/></h1>
        <p class="lead"><spring:message code="home.description"/></p>
        <div class="button-row">
            <sec:authorize access="isAnonymous()">
                <c:url var="loginUrl" value="/login"/>
                <a class="button button-large" href="${loginUrl}"><spring:message code="home.action.login"/></a>
            </sec:authorize>
            <sec:authorize access="isAuthenticated()">
                <c:url var="workspaceUrl" value="/workspace"/>
                <a class="button button-large" href="${workspaceUrl}"><spring:message code="home.action.workspace"/></a>
            </sec:authorize>
        </div>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
