<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="pageTitle" value="Plantios" />
<c:set var="activeMenu" value="plantings" />
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
				<h1 class="page-title">Plantios</h1>
				<p class="page-sub">O que está plantado, onde e até quando.</p>
			</div>
			<div class="page-actions">
				<a class="btn btn-primary" href="${ctx}/planting/form"><ui:icon name="plus" />Novo plantio</a>
			</div>
		</div>

		<section class="panel" aria-label="Lista de plantios">
			<div class="toolbar">
				<%-- Abas por situação: cada uma mantém a busca atual e mostra quantos plantios tem --%>
				<nav class="tabs" aria-label="Filtrar por situação">
					<c:url var="allUrl" value="/plantings"><c:param name="q" value="${search}" /></c:url>
					<a class="tab" href="<c:out value='${allUrl}'/>" ${empty status ? 'aria-current="true"' : ''}>
						Todos <span class="tab-count">${allCount}</span>
					</a>
					<c:forEach var="s" items="${statuses}">
						<c:url var="tabUrl" value="/plantings">
							<c:param name="q" value="${search}" />
							<c:param name="status" value="${s.name()}" />
						</c:url>
						<a class="tab" href="<c:out value='${tabUrl}'/>" ${status eq s.name() ? 'aria-current="true"' : ''}>
							<c:out value="${s.label}" /> <span class="tab-count">${empty statusCounts[s] ? 0 : statusCounts[s]}</span>
						</a>
					</c:forEach>
				</nav>

				<form class="search" method="get" action="${ctx}/plantings" role="search">
					<c:if test="${not empty status}"><input type="hidden" name="status" value="<c:out value='${status}'/>"></c:if>
					<div class="search-field">
						<ui:icon name="search" />
						<input class="input" type="search" name="q" value="<c:out value='${search}'/>" maxlength="100"
							placeholder="Cultura, propriedade ou responsável" aria-label="Buscar plantios">
					</div>
					<button class="btn btn-secondary" type="submit">Buscar</button>
					<c:if test="${not empty search}">
						<c:url var="clearUrl" value="/plantings"><c:param name="status" value="${status}" /></c:url>
						<a class="search-clear" href="<c:out value='${clearUrl}'/>">Limpar</a>
					</c:if>
				</form>
			</div>

			<c:choose>
				<c:when test="${empty plantings}">
					<div class="empty">
						<span class="empty-icon"><ui:icon name="sprout" /></span>
						<c:choose>
							<c:when test="${not empty search or not empty status}">
								<p class="empty-title">Nenhum plantio encontrado</p>
								<p class="empty-text">Nada combina com esta busca. Tente outro termo ou escolha outra situação.</p>
								<a class="btn btn-secondary" href="${ctx}/plantings">Ver todos os plantios</a>
							</c:when>
							<c:otherwise>
								<p class="empty-title">Nenhum plantio cadastrado</p>
								<p class="empty-text">Registre o primeiro plantio para acompanhar a safra no painel.</p>
								<a class="btn btn-primary" href="${ctx}/planting/form"><ui:icon name="plus" />Novo plantio</a>
							</c:otherwise>
						</c:choose>
					</div>
				</c:when>
				<c:otherwise>
					<div class="table-wrap">
						<table class="data-table">
							<thead>
								<tr>
									<th scope="col">Cultura</th>
									<th scope="col">Propriedade</th>
									<th scope="col">Período</th>
									<th scope="col" class="num">Área e produção</th>
									<th scope="col">Situação</th>
									<th scope="col">Responsável</th>
									<th scope="col"><span class="sr-only">Ações</span></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="p" items="${plantings}">
									<c:url var="editUrl" value="/planting/update">
										<c:param name="id" value="${p.id}" />
										<c:param name="back" value="${pagination.query}" />
									</c:url>
									<tr>
										<td data-label="Cultura">
											<span class="cell-title"><c:out value="${p.crop}" /></span>
											<c:if test="${not empty p.variety}"><span class="cell-sub"><c:out value="${p.variety}" /></span></c:if>
										</td>
										<td data-label="Propriedade" class="cell-wide">
											<span class="cell-title"><c:out value="${p.property.name}" /></span>
											<span class="cell-sub"><c:out value="${p.property.location}" /></span>
										</td>
										<td data-label="Período">
											<span class="cell-nowrap"><fmt:formatDate value="${p.plantingDate}" pattern="dd/MM/yyyy" /> a <fmt:formatDate value="${p.expectedHarvest}" pattern="dd/MM/yyyy" /></span>
											<c:if test="${p.status.name() eq 'GROWING'}">
												<span class="cell-sub${p.daysToHarvest < 0 ? ' late' : ''}">Colheita <c:out value="${p.harvestCountdown}" /></span>
											</c:if>
										</td>
										<td data-label="Área e produção" class="num">
											<span class="cell-title"><fmt:formatNumber value="${p.area}" maxFractionDigits="2" /> ha</span>
											<span class="cell-sub"><fmt:formatNumber value="${p.estimatedBags}" maxFractionDigits="0" /> sacas</span>
										</td>
										<td data-label="Situação"><span class="chip chip-${p.status.cssClass}"><c:out value="${p.status.label}" /></span></td>
										<td data-label="Responsável"><c:out value="${p.manager.name}" /></td>
										<td class="cell-actions">
											<a class="btn-icon" href="<c:out value='${editUrl}'/>" title="Editar" aria-label="Editar plantio de ${fn:escapeXml(p.crop)}"><ui:icon name="pencil" /></a>
											<button type="button" class="btn-icon btn-icon-danger" title="Excluir" aria-label="Excluir plantio de ${fn:escapeXml(p.crop)}"
												data-delete data-action="${ctx}/planting/delete" data-id="${p.id}" data-kind="plantio"
												data-name="<c:out value='${p.crop}'/> em <c:out value='${p.property.name}'/>"><ui:icon name="trash-2" /></button>
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
