<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Conteúdo comum do <head> de todas as páginas (incluído com <%@ include file="base-head.jsp" %>).
     Cada página define antes a variável "pageTitle". "ctx" é o nome do projeto no Tomcat
     (ex.: /crud-manager): usar a variável evita escrever o nome do projeto à mão nas páginas. --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${empty pageTitle ? 'CRUD Manager' : pageTitle}"/><c:if test="${not empty pageTitle}"> | CRUD Manager</c:if></title>
<link rel="icon" type="image/svg+xml" href="${ctx}/images/favicon.svg">
<link rel="preload" href="${ctx}/fonts/zilla-slab-latin-600-normal.woff2" as="font" type="font/woff2" crossorigin>
<link rel="preload" href="${ctx}/fonts/source-sans-3-latin-wght-normal.woff2" as="font" type="font/woff2" crossorigin>
<link rel="stylesheet" href="${ctx}/css/app.css">
<script src="${ctx}/js/app.js" defer></script>
