<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="pageTitle" value="Propriedades" />
<c:set var="activeMenu" value="properties" />
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
				<h1 class="page-title">Propriedades rurais</h1>
				<p class="page-sub">Fazendas, sítios, chácaras e granjas, com a área e o dono de cada uma.</p>
			</div>
			<div class="page-actions">
				<a class="btn btn-primary" href="${ctx}/property/form"><ui:icon name="plus" />Nova propriedade</a>
			</div>
		</div>

		<section class="panel" aria-label="Lista de propriedades">
			<div class="toolbar">
				<form class="search" method="get" action="${ctx}/properties" role="search">
					<div class="search-field">
						<ui:icon name="search" />
						<input class="input" type="search" name="q" value="<c:out value='${search}'/>" maxlength="100"
							placeholder="Buscar por nome, cidade, estado ou dono" aria-label="Buscar propriedades">
					</div>
					<button class="btn btn-secondary" type="submit">Buscar</button>
					<c:if test="${not empty search}">
						<a class="search-clear" href="${ctx}/properties">Limpar</a>
					</c:if>
				</form>
			</div>

			<c:choose>
				<c:when test="${empty properties}">
					<div class="empty">
						<span class="empty-icon"><ui:icon name="land-plot" /></span>
						<c:choose>
							<c:when test="${not empty search}">
								<p class="empty-title">Nenhuma propriedade encontrada</p>
								<p class="empty-text">Nada combina com esta busca. Tente outro nome, cidade ou dono.</p>
								<a class="btn btn-secondary" href="${ctx}/properties">Ver todas as propriedades</a>
							</c:when>
							<c:otherwise>
								<p class="empty-title">Nenhuma propriedade cadastrada</p>
								<p class="empty-text">Cadastre a primeira propriedade para poder registrar plantios nela.</p>
								<a class="btn btn-primary" href="${ctx}/property/form"><ui:icon name="plus" />Nova propriedade</a>
							</c:otherwise>
						</c:choose>
					</div>
				</c:when>
				<c:otherwise>
					<div class="table-wrap">
						<table class="data-table">
							<thead>
								<tr>
									<th scope="col">Propriedade</th>
									<th scope="col">Local</th>
									<th scope="col" class="num">Área</th>
									<th scope="col">Irrigação</th>
									<th scope="col">Dono</th>
									<th scope="col" class="num">Plantios</th>
									<th scope="col"><span class="sr-only">Ações</span></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="p" items="${properties}">
									<c:url var="editUrl" value="/property/update">
										<c:param name="id" value="${p.id}" />
										<c:param name="back" value="${pagination.query}" />
									</c:url>
									<c:url var="plantingsUrl" value="/plantings"><c:param name="q" value="${p.name}" /></c:url>
									<tr>
										<td data-label="Propriedade">
											<span class="cell-title"><c:out value="${p.name}" /></span>
											<span class="cell-sub"><span class="tag"><c:out value="${p.type.label}" /></span>
												<c:if test="${not empty p.acquiredOn}"> desde <fmt:formatDate value="${p.acquiredOn}" pattern="MM/yyyy" /></c:if></span>
										</td>
										<td data-label="Local"><c:out value="${p.location}" /></td>
										<td data-label="Área" class="num"><fmt:formatNumber value="${p.area}" maxFractionDigits="2" /> ha</td>
										<td data-label="Irrigação">
											<c:choose>
												<c:when test="${p.irrigated}"><span class="flag flag-on"><ui:icon name="droplets" />Irrigada</span></c:when>
												<c:otherwise><span class="flag flag-off">Sequeiro</span></c:otherwise>
											</c:choose>
										</td>
										<td data-label="Dono"><c:out value="${p.owner.name}" /></td>
										<td data-label="Plantios" class="num">
											<c:choose>
												<c:when test="${p.plantingCount > 0}">
													<a class="link-count" href="<c:out value='${plantingsUrl}'/>">${p.plantingCount}</a>
												</c:when>
												<c:otherwise><span class="muted">0</span></c:otherwise>
											</c:choose>
										</td>
										<td class="cell-actions">
											<c:if test="${not empty p.mapUrl}">
												<a class="btn-icon" href="<c:out value='${p.mapUrl}'/>" target="_blank" rel="noopener noreferrer"
													title="Ver no mapa" aria-label="Ver ${fn:escapeXml(p.name)} no mapa (abre em nova aba)"><ui:icon name="map-pin" /></a>
											</c:if>
											<a class="btn-icon" href="<c:out value='${editUrl}'/>" title="Editar" aria-label="Editar ${fn:escapeXml(p.name)}"><ui:icon name="pencil" /></a>
											<button type="button" class="btn-icon btn-icon-danger" title="Excluir" aria-label="Excluir ${fn:escapeXml(p.name)}"
												data-delete data-action="${ctx}/property/delete" data-id="${p.id}" data-kind="propriedade"
												data-name="<c:out value='${p.name}'/>"
												<c:if test="${p.plantingCount > 0}">data-blocked="<c:out value='${p.name}'/> tem ${p.plantingCount} ${p.plantingCount == 1 ? 'plantio' : 'plantios'} e não pode ser excluída. Exclua ou mude os plantios antes."</c:if>><ui:icon name="trash-2" /></button>
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
