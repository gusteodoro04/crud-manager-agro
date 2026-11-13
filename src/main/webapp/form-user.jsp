<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="isUpdate" value="${action eq 'update'}" />
<c:set var="pageTitle" value="${isUpdate ? 'Editar usuário' : 'Novo usuário'}" />
<c:set var="activeMenu" value="users" />
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
				<h1 class="page-title">${isUpdate ? 'Editar usuário' : 'Novo usuário'}</h1>
				<p class="page-sub">${isUpdate ? 'Atualize os dados e salve.' : 'O e-mail e a senha serão usados para entrar no sistema.'}</p>
			</div>
		</div>

		<form class="panel form-panel" method="post" action="${ctx}/user/${action}" autocomplete="off">
			<%@ include file="/WEB-INF/fragments/csrf.jspf"%>
			<c:if test="${isUpdate}"><input type="hidden" name="id" value="<c:out value='${form.id}'/>"></c:if>
			<input type="hidden" name="back" value="<c:out value='${back}'/>">

			<c:if test="${not empty errors}">
				<div class="form-error" role="alert">
					<ui:icon name="circle-alert" />
					<span>Revise ${fn:length(errors) == 1 ? 'o campo marcado' : 'os campos marcados'} abaixo.</span>
				</div>
			</c:if>

			<ui:field name="name" label="Nome completo">
				<input class="input" type="text" id="name" name="name" maxlength="150" required autocomplete="off"
					value="<c:out value='${form.name}'/>"
					aria-invalid="${not empty errors.name}" aria-describedby="name-error">
			</ui:field>

			<ui:field name="gender" label="Sexo" group="true">
				<div class="segmented" role="radiogroup" aria-labelledby="gender-label">
					<label class="seg">
						<input type="radio" name="gender" value="F" ${form.gender eq 'F' ? 'checked' : ''} required>
						<span class="seg-body">Feminino</span>
					</label>
					<label class="seg">
						<input type="radio" name="gender" value="M" ${form.gender eq 'M' ? 'checked' : ''} required>
						<span class="seg-body">Masculino</span>
					</label>
				</div>
			</ui:field>

			<ui:field name="mail" label="E-mail" hint="É o login da pessoa. Não pode repetir.">
				<input class="input" type="email" id="mail" name="mail" maxlength="150" required autocomplete="off"
					value="<c:out value='${form.mail}'/>"
					aria-invalid="${not empty errors.mail}" aria-describedby="mail-hint mail-error">
			</ui:field>

			<ui:field name="password" label="Senha" optional="${isUpdate}"
				hint="${isUpdate ? 'Deixe em branco para manter a senha atual.' : 'Pelo menos 6 caracteres.'}">
				<div class="input-group">
					<input class="input" type="password" id="password" name="password" minlength="6" maxlength="100" autocomplete="new-password"
						${isUpdate ? '' : 'required'}
						aria-invalid="${not empty errors.password}" aria-describedby="password-hint password-error">
					<button type="button" class="btn-icon" data-toggle-password aria-controls="password"
						aria-label="Mostrar senha" aria-pressed="false">
						<span data-icon-show><ui:icon name="eye" /></span>
						<span data-icon-hide hidden><ui:icon name="eye-off" /></span>
					</button>
				</div>
			</ui:field>

			<div class="form-actions">
				<a class="btn btn-secondary" href="${ctx}/users${empty back ? '' : '?'}<c:out value='${back}'/>">Cancelar</a>
				<button class="btn btn-primary" type="submit"><ui:icon name="check" />${isUpdate ? 'Salvar alterações' : 'Cadastrar usuário'}</button>
			</div>
		</form>
	</main>
</body>
</html>
