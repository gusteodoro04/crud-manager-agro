<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="pageTitle" value="Entrar" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
	<%@ include file="base-head.jsp"%>
</head>
<body>
	<div class="login">

		<%-- Lado esquerdo: a marca e a arte (linhas de plantio em curva de nível) --%>
		<section class="login-hero" aria-label="Apresentação">
			<img class="login-rows" src="${ctx}/images/login-rows.svg" alt="" width="800" height="1000" decoding="async">
			<a class="brand" href="${ctx}/">
				<span class="brand-mark"><ui:icon name="sprout" /></span>
				CRUD Manager
			</a>
			<div>
				<p class="login-claim">Cada talhão no seu lugar.</p>
				<p class="login-claim-sub">Propriedades, plantios e responsáveis num só lugar, da semeadura à colheita.</p>
			</div>
		</section>

		<%-- Lado direito: o formulário --%>
		<main class="login-main" id="conteudo">
			<div class="login-card">
				<h1 class="login-title">Entrar</h1>
				<p class="login-lead">Use o e-mail e a senha do seu cadastro.</p>

				<c:if test="${not empty loginError}">
					<div class="login-alert" role="alert">
						<ui:icon name="circle-alert" />
						<span><c:out value="${loginError}" /></span>
					</div>
				</c:if>
				<c:if test="${not empty info}">
					<div class="login-alert is-info" role="status">
						<ui:icon name="info" />
						<span><c:out value="${info}" /></span>
					</div>
				</c:if>

				<form method="post" action="${ctx}/login">
					<%@ include file="/WEB-INF/fragments/csrf.jspf"%>

					<div class="login-field">
						<label class="field-label" for="email">E-mail</label>
						<input class="input" id="email" name="email" type="email" maxlength="150"
							autocomplete="username" required autofocus value="<c:out value='${email}'/>">
					</div>

					<div class="login-field">
						<label class="field-label" for="password">Senha</label>
						<div class="input-group">
							<input class="input" id="password" name="password" type="password" maxlength="200"
								autocomplete="current-password" required>
							<button type="button" class="btn-icon" data-toggle-password aria-controls="password"
								aria-label="Mostrar senha" aria-pressed="false">
								<span data-icon-show><ui:icon name="eye" /></span>
								<span data-icon-hide hidden><ui:icon name="eye-off" /></span>
							</button>
						</div>
					</div>

					<button class="btn btn-primary login-submit" type="submit">Entrar</button>
				</form>

				<%-- Acesso de teste para a apresentação do trabalho. Em um sistema de verdade,
				     apague este bloco. --%>
				<div class="demo-box">
					<p>Acesso de teste: <code>emerson@mail.com</code> com a senha <code>agro123</code></p>
					<button type="button" class="btn btn-secondary" data-fill data-email="emerson@mail.com"
						data-password="agro123">Preencher os campos</button>
				</div>
			</div>
		</main>
	</div>
</body>
</html>
