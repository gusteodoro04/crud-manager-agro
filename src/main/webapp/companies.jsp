<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="pageTitle" value="Empresas" />
<c:set var="activeMenu" value="companies" />
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
				<h1 class="page-title">Empresas</h1>
				<p class="page-sub">Onde cada pessoa trabalha ou já trabalhou, e em que cargo.</p>
			</div>
			<div class="page-actions">
				<a class="btn btn-primary" href="${ctx}/company/form"><ui:icon name="plus" />Nova empresa</a>
			</div>
		</div>

		<section class="panel" aria-label="Lista de empresas">
			<div class="toolbar">
				<form class="search" method="get" action="${ctx}/companies" role="search">
					<div class="search-field">
						<ui:icon name="search" />
						<input class="input" type="search" name="q" value="<c:out value='${search}'/>" maxlength="100"
							placeholder="Buscar por empresa, cargo ou pessoa" aria-label="Buscar empresas">
					</div>
					<button class="btn btn-secondary" type="submit">Buscar</button>
					<c:if test="${not empty search}">
						<a class="search-clear" href="${ctx}/companies">Limpar</a>
					</c:if>
				</form>
			</div>

			<c:choose>
				<c:when test="${empty companies}">
					<div class="empty">
						<span class="empty-icon"><ui:icon name="building-2" /></span>
						<c:choose>
							<c:when test="${not empty search}">
								<p class="empty-title">Nenhuma empresa encontrada</p>
								<p class="empty-text">Nada combina com esta busca. Tente outro nome, cargo ou pessoa.</p>
								<a class="btn btn-secondary" href="${ctx}/companies">Ver todas as empresas</a>
							</c:when>
							<c:otherwise>
								<p class="empty-title">Nenhuma empresa cadastrada</p>
								<p class="empty-text">Cadastre a primeira empresa de um usuário.</p>
								<a class="btn btn-primary" href="${ctx}/company/form"><ui:icon name="plus" />Nova empresa</a>
							</c:otherwise>
						</c:choose>
					</div>
				</c:when>
				<c:otherwise>
					<div class="table-wrap">
						<table class="data-table">
							<thead>
								<tr>
									<th scope="col">Empresa</th>
									<th scope="col">Cargo</th>
									<th scope="col">Período</th>
									<th scope="col">Pessoa</th>
									<th scope="col"><span class="sr-only">Ações</span></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="co" items="${companies}">
									<c:url var="editUrl" value="/company/update">
										<c:param name="id" value="${co.id}" />
										<c:param name="back" value="${pagination.query}" />
									</c:url>
									<tr>
										<td data-label="Empresa"><span class="cell-title"><c:out value="${co.name}" /></span></td>
										<td data-label="Cargo"><c:out value="${co.role}" /></td>
										<td data-label="Período">
											<fmt:formatDate value="${co.start}" pattern="MM/yyyy" /> a
											<c:choose>
												<c:when test="${empty co.end}"><span class="flag flag-on">atual</span></c:when>
												<c:otherwise><fmt:formatDate value="${co.end}" pattern="MM/yyyy" /></c:otherwise>
											</c:choose>
										</td>
										<td data-label="Pessoa" class="cell-wide">
											<span class="person">
												<span class="avatar" aria-hidden="true"><c:out value="${co.user.initials}" /></span>
												<span><c:out value="${co.user.name}" /></span>
											</span>
										</td>
										<td class="cell-actions">
											<a class="btn-icon" href="<c:out value='${editUrl}'/>" title="Editar" aria-label="Editar ${fn:escapeXml(co.name)}"><ui:icon name="pencil" /></a>
											<button type="button" class="btn-icon btn-icon-danger" title="Excluir" aria-label="Excluir ${fn:escapeXml(co.name)}"
												data-delete data-action="${ctx}/company/delete" data-id="${co.id}" data-kind="empresa"
												data-name="<c:out value='${co.name}'/>"><ui:icon name="trash-2" /></button>
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
