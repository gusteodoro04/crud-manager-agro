<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="pageTitle" value="Usuários" />
<c:set var="activeMenu" value="users" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
	<%@ include file="base-head.jsp"%>
</head>
<body>
	<%@ include file="nav-menu.jsp"%>
	<%@ include file="/WEB-INF/fragments/flash.jspf"%>
	<%@ include file="/WEB-INF/fragments/delete-dialog.jspf"%>

	<main id="conteudo" class="page">
		<div class="page-head">
			<div>
				<h1 class="page-title">Usuários</h1>
				<p class="page-sub">Quem tem acesso ao sistema. O e-mail é o login de cada pessoa.</p>
			</div>
			<div class="page-actions">
				<a class="btn btn-primary" href="${ctx}/user/form"><ui:icon name="plus" />Novo usuário</a>
			</div>
		</div>

		<section class="panel" aria-label="Lista de usuários">
			<div class="toolbar">
				<form class="search" method="get" action="${ctx}/users" role="search">
					<div class="search-field">
						<ui:icon name="search" />
						<input class="input" type="search" name="q" value="<c:out value='${search}'/>" maxlength="100"
							placeholder="Buscar por nome ou e-mail" aria-label="Buscar usuários">
					</div>
					<button class="btn btn-secondary" type="submit">Buscar</button>
					<c:if test="${not empty search}">
						<a class="search-clear" href="${ctx}/users">Limpar</a>
					</c:if>
				</form>
			</div>

			<c:choose>
				<c:when test="${empty users}">
					<div class="empty">
						<span class="empty-icon"><ui:icon name="users" /></span>
						<c:choose>
							<c:when test="${not empty search}">
								<p class="empty-title">Nenhum usuário encontrado</p>
								<p class="empty-text">Nada combina com esta busca. Tente outro nome ou e-mail.</p>
								<a class="btn btn-secondary" href="${ctx}/users">Ver todos os usuários</a>
							</c:when>
							<c:otherwise>
								<p class="empty-title">Nenhum usuário cadastrado</p>
								<p class="empty-text">Cadastre o primeiro usuário.</p>
								<a class="btn btn-primary" href="${ctx}/user/form"><ui:icon name="plus" />Novo usuário</a>
							</c:otherwise>
						</c:choose>
					</div>
				</c:when>
				<c:otherwise>
					<div class="table-wrap">
						<table class="data-table">
							<thead>
								<tr>
									<th scope="col">Nome</th>
									<th scope="col">Sexo</th>
									<th scope="col">E-mail</th>
									<th scope="col"><span class="sr-only">Ações</span></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="u" items="${users}">
									<c:url var="editUrl" value="/user/update">
										<c:param name="id" value="${u.id}" />
										<c:param name="back" value="${pagination.query}" />
									</c:url>
									<tr>
										<td data-label="Nome">
											<span class="person">
												<span class="avatar" aria-hidden="true"><c:out value="${u.initials}" /></span>
												<span>
													<span class="cell-title"><c:out value="${u.name}" /></span>
													<c:if test="${u.id == loggedUser.id}"><span class="cell-sub">Você</span></c:if>
												</span>
											</span>
										</td>
										<td data-label="Sexo"><c:out value="${u.gender eq 'F' ? 'Feminino' : (u.gender eq 'M' ? 'Masculino' : '')}" /></td>
										<td data-label="E-mail" class="cell-wide"><c:out value="${u.email}" /></td>
										<td class="cell-actions">
											<a class="btn-icon" href="<c:out value='${editUrl}'/>" title="Editar" aria-label="Editar ${fn:escapeXml(u.name)}"><ui:icon name="pencil" /></a>
											<button type="button" class="btn-icon btn-icon-danger" title="Excluir" aria-label="Excluir ${fn:escapeXml(u.name)}"
												data-delete data-action="${ctx}/user/delete" data-id="${u.id}" data-kind="usuário"
												data-name="<c:out value='${u.name}'/>"
												<c:if test="${u.id == loggedUser.id}">data-blocked="Você não pode excluir o usuário com o qual está conectado."</c:if>><ui:icon name="trash-2" /></button>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</c:otherwise>
			</c:choose>

			<%@ include file="/WEB-INF/fragments/pagination.jspf"%>
		</section>
	</main>
</body>
</html>
