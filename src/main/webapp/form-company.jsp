<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="isUpdate" value="${action eq 'update'}" />
<c:set var="pageTitle" value="${isUpdate ? 'Editar empresa' : 'Nova empresa'}" />
<c:set var="activeMenu" value="companies" />
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
				<h1 class="page-title">${isUpdate ? 'Editar empresa' : 'Nova empresa'}</h1>
				<p class="page-sub">${isUpdate ? 'Atualize os dados e salve.' : 'Registre onde a pessoa trabalha ou trabalhou.'}</p>
			</div>
		</div>

		<form class="panel form-panel" method="post" action="${ctx}/company/${action}">
			<%@ include file="/WEB-INF/fragments/csrf.jspf"%>
			<c:if test="${isUpdate}"><input type="hidden" name="id" value="<c:out value='${form.id}'/>"></c:if>
			<input type="hidden" name="back" value="<c:out value='${back}'/>">

			<c:if test="${not empty errors}">
				<div class="form-error" role="alert">
					<ui:icon name="circle-alert" />
					<span>Revise ${fn:length(errors) == 1 ? 'o campo marcado' : 'os campos marcados'} abaixo.</span>
				</div>
			</c:if>

			<ui:field name="name" label="Empresa">
				<input class="input" type="text" id="name" name="name" maxlength="128" required
					value="<c:out value='${form.name}'/>"
					aria-invalid="${not empty errors.name}" aria-describedby="name-error">
			</ui:field>

			<ui:field name="role" label="Cargo">
				<input class="input" type="text" id="role" name="role" maxlength="128" required
					value="<c:out value='${form.role}'/>"
					aria-invalid="${not empty errors.role}" aria-describedby="role-error">
			</ui:field>

			<ui:field name="start" label="Início">
				<input class="input input-short" type="date" id="start" name="start" required data-min-source
					value="<c:out value='${form.start}'/>"
					aria-invalid="${not empty errors.start}" aria-describedby="start-error">
			</ui:field>

			<ui:field name="end" label="Saída" hint="Deixe em branco se a pessoa ainda trabalha lá." optional="true">
				<input class="input input-short" type="date" id="end" name="end" data-min-target
					value="<c:out value='${form.end}'/>"
					aria-invalid="${not empty errors.end}" aria-describedby="end-hint end-error">
			</ui:field>

			<ui:field name="user" label="Pessoa" hint="Usuário que trabalha (ou trabalhou) na empresa.">
				<select class="select" id="user" name="user" required
					aria-invalid="${not empty errors.user}" aria-describedby="user-hint user-error">
					<option value="" disabled ${empty form.user ? 'selected' : ''}>Selecione a pessoa</option>
					<c:forEach var="u" items="${users}">
						<option value="${u.id}" ${form.user eq u.id.toString() ? 'selected' : ''}><c:out value="${u.name}" /></option>
					</c:forEach>
				</select>
			</ui:field>

			<div class="form-actions">
				<a class="btn btn-secondary" href="${ctx}/companies${empty back ? '' : '?'}<c:out value='${back}'/>">Cancelar</a>
				<button class="btn btn-primary" type="submit"><ui:icon name="check" />${isUpdate ? 'Salvar alterações' : 'Cadastrar empresa'}</button>
			</div>
		</form>
	</main>
</body>
</html>
