<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="isUpdate" value="${action eq 'update'}" />
<c:set var="pageTitle" value="${isUpdate ? 'Editar post' : 'Novo post'}" />
<c:set var="activeMenu" value="posts" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
	<%@ include file="base-head.jsp"%>
</head>
<body>
	<%@ include file="nav-menu.jsp"%>
	<%@ include file="/WEB-INF/fragments/flash.jspf"%>

	<main id="conteudo" class="page">
		<div class="page-head">
			<div>
				<h1 class="page-title">${isUpdate ? 'Editar post' : 'Novo post'}</h1>
				<p class="page-sub">${isUpdate ? 'Atualize o texto e salve.' : 'Escreva um recado para a equipe.'}</p>
			</div>
		</div>

		<form class="panel form-panel" method="post" action="${ctx}/post/${action}">
			<%@ include file="/WEB-INF/fragments/csrf.jspf"%>
			<c:if test="${isUpdate}"><input type="hidden" name="id" value="<c:out value='${form.id}'/>"></c:if>
			<input type="hidden" name="back" value="<c:out value='${back}'/>">

			<c:if test="${not empty errors}">
				<div class="form-error" role="alert">
					<ui:icon name="circle-alert" />
					<span>Revise ${fn:length(errors) == 1 ? 'o campo marcado' : 'os campos marcados'} abaixo.</span>
				</div>
			</c:if>

			<ui:field name="content" label="Texto" hint="Até 1.000 caracteres.">
				<textarea class="textarea" id="content" name="content" rows="6" maxlength="1000" required
					aria-invalid="${not empty errors.content}" aria-describedby="content-hint content-error"><c:out value="${form.content}" /></textarea>
			</ui:field>

			<ui:field name="user" label="Autor" hint="Quem escreve o post.">
				<select class="select" id="user" name="user" required
					aria-invalid="${not empty errors.user}" aria-describedby="user-hint user-error">
					<option value="" disabled ${empty form.user ? 'selected' : ''}>Selecione o autor</option>
					<c:forEach var="u" items="${users}">
						<option value="${u.id}" ${form.user eq u.id.toString() ? 'selected' : ''}><c:out value="${u.name}" /></option>
					</c:forEach>
				</select>
			</ui:field>

			<div class="form-actions">
				<a class="btn btn-secondary" href="${ctx}/posts${empty back ? '' : '?'}<c:out value='${back}'/>">Cancelar</a>
				<button class="btn btn-primary" type="submit"><ui:icon name="check" />${isUpdate ? 'Salvar alterações' : 'Publicar post'}</button>
			</div>
		</form>
	</main>
</body>
</html>
