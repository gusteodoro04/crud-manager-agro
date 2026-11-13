<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<%-- Página de erro (configurada no web.xml para 403, 404, 405, 500 e exceções). Não mostra
     detalhes técnicos: eles ficam só no log do servidor. --%>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}" />
<c:choose>
	<c:when test="${code == 404}">
		<c:set var="pageTitle" value="Página não encontrada" />
		<c:set var="errorTitle" value="Não encontramos esta página" />
		<c:set var="errorText" value="O endereço pode ter sido digitado errado ou o registro não existe mais." />
	</c:when>
	<c:when test="${code == 403}">
		<c:set var="pageTitle" value="Ação não confirmada" />
		<c:set var="errorTitle" value="Não foi possível confirmar esta ação" />
		<c:set var="errorText" value="A página ficou desatualizada ou a sua sessão mudou. Volte ao início e tente de novo." />
	</c:when>
	<c:when test="${code == 405}">
		<c:set var="pageTitle" value="Ação não permitida" />
		<c:set var="errorTitle" value="Esta ação não é permitida aqui" />
		<c:set var="errorText" value="Volte ao início e use os botões do sistema." />
	</c:when>
	<c:otherwise>
		<c:set var="pageTitle" value="Algo deu errado" />
		<c:set var="errorTitle" value="Algo deu errado do nosso lado" />
		<c:set var="errorText" value="Tente de novo em instantes. Se o problema continuar, avise quem cuida do sistema." />
	</c:otherwise>
</c:choose>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
	<%@ include file="base-head.jsp"%>
</head>
<body>
	<c:choose>
		<c:when test="${not empty loggedUser}">
			<%@ include file="nav-menu.jsp"%>
		</c:when>
		<c:otherwise>
			<header class="topbar">
				<div class="topbar-inner">
					<a class="brand" href="${ctx}/login"><span class="brand-mark"><ui:icon name="sprout" /></span>CRUD Manager</a>
				</div>
			</header>
		</c:otherwise>
	</c:choose>

	<main class="error-page" id="conteudo">
		<div>
			<p class="error-code"><c:out value="${empty code ? 500 : code}" /></p>
			<h1 class="error-title"><c:out value="${errorTitle}" /></h1>
			<p class="error-text"><c:out value="${errorText}" /></p>
			<a class="btn btn-primary" href="${ctx}/"><ui:icon name="arrow-left" />Voltar ao início</a>
		</div>
	</main>
</body>
</html>
