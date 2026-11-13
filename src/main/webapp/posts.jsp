<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="pageTitle" value="Posts" />
<c:set var="activeMenu" value="posts" />
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
				<h1 class="page-title">Posts</h1>
				<p class="page-sub">Recados e anotações da equipe, do mais recente para o mais antigo.</p>
			</div>
			<div class="page-actions">
				<a class="btn btn-primary" href="${ctx}/post/form"><ui:icon name="plus" />Novo post</a>
			</div>
		</div>

		<section class="panel" aria-label="Lista de posts">
			<div class="toolbar">
				<form class="search" method="get" action="${ctx}/posts" role="search">
					<div class="search-field">
						<ui:icon name="search" />
						<input class="input" type="search" name="q" value="<c:out value='${search}'/>" maxlength="100"
							placeholder="Buscar no texto ou pelo autor" aria-label="Buscar posts">
					</div>
					<button class="btn btn-secondary" type="submit">Buscar</button>
					<c:if test="${not empty search}">
						<a class="search-clear" href="${ctx}/posts">Limpar</a>
					</c:if>
				</form>
			</div>

			<c:choose>
				<c:when test="${empty posts}">
					<div class="empty">
						<span class="empty-icon"><ui:icon name="message-square-text" /></span>
						<c:choose>
							<c:when test="${not empty search}">
								<p class="empty-title">Nenhum post encontrado</p>
								<p class="empty-text">Nada combina com esta busca. Tente outro termo.</p>
								<a class="btn btn-secondary" href="${ctx}/posts">Ver todos os posts</a>
							</c:when>
							<c:otherwise>
								<p class="empty-title">Nenhum post ainda</p>
								<p class="empty-text">Escreva o primeiro post para deixar um recado para a equipe.</p>
								<a class="btn btn-primary" href="${ctx}/post/form"><ui:icon name="plus" />Novo post</a>
							</c:otherwise>
						</c:choose>
					</div>
				</c:when>
				<c:otherwise>
					<div class="table-wrap">
						<table class="data-table">
							<thead>
								<tr>
									<th scope="col">Post</th>
									<th scope="col">Autor</th>
									<th scope="col" class="num">Data</th>
									<th scope="col"><span class="sr-only">Ações</span></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="p" items="${posts}">
									<c:url var="editUrl" value="/post/update">
										<c:param name="id" value="${p.id}" />
										<c:param name="back" value="${pagination.query}" />
									</c:url>
									<tr>
										<td data-label="Post"><span class="cell-clamp"><c:out value="${p.content}" /></span></td>
										<td data-label="Autor" class="cell-wide">
											<span class="person">
												<span class="avatar" aria-hidden="true"><c:out value="${p.user.initials}" /></span>
												<span><c:out value="${p.user.name}" /></span>
											</span>
										</td>
										<td data-label="Data" class="num"><fmt:formatDate value="${p.postDate}" pattern="dd/MM/yyyy" /></td>
										<td class="cell-actions">
											<a class="btn-icon" href="<c:out value='${editUrl}'/>" title="Editar" aria-label="Editar post de ${fn:escapeXml(p.user.name)}"><ui:icon name="pencil" /></a>
											<button type="button" class="btn-icon btn-icon-danger" title="Excluir" aria-label="Excluir post de ${fn:escapeXml(p.user.name)}"
												data-delete data-action="${ctx}/post/delete" data-id="${p.id}" data-kind="post"
												data-name="<c:out value='${fn:substring(p.content, 0, 60)}'/><c:if test='${fn:length(p.content) > 60}'>…</c:if>"><ui:icon name="trash-2" /></button>
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
