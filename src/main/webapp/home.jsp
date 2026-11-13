<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="pageTitle" value="Início" />
<c:set var="activeMenu" value="home" />
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
				<h1 class="page-title"><c:out value="${greeting}" />, <c:out value="${loggedUser.firstName}" /></h1>
				<p class="page-sub">
					Safra <c:out value="${calendar.label}" />. Hoje é <fmt:formatDate value="${now}" pattern="EEEE, d 'de' MMMM" />.
				</p>
			</div>
			<div class="page-actions">
				<a class="btn btn-secondary" href="${ctx}/property/form"><ui:icon name="land-plot" />Nova propriedade</a>
				<a class="btn btn-primary" href="${ctx}/planting/form"><ui:icon name="plus" />Novo plantio</a>
			</div>
		</div>

		<c:if test="${empty summary}">
			<div class="panel panel-empty">Não foi possível carregar os números agora.</div>
		</c:if>

		<c:if test="${not empty summary}">

			<%-- Situação dos plantios: a barra mostra a proporção; cada número leva à lista filtrada --%>
			<section class="panel home-state" aria-labelledby="state-title">
				<h2 class="sr-only" id="state-title">Situação dos plantios</h2>

				<c:choose>
					<c:when test="${summary.plantingCount > 0}">
						<div class="status-bar" role="img"
							aria-label="${summary.plantingCount} plantios: ${summary.plannedCount} planejados, ${summary.growingCount} em desenvolvimento, ${summary.harvestedCount} colhidos e ${summary.lostCount} perdidos">
							<c:if test="${summary.plannedCount > 0}"><span class="status-seg seg-planned" style="flex-grow: ${summary.plannedCount}"></span></c:if>
							<c:if test="${summary.growingCount > 0}"><span class="status-seg seg-growing" style="flex-grow: ${summary.growingCount}"></span></c:if>
							<c:if test="${summary.harvestedCount > 0}"><span class="status-seg seg-harvested" style="flex-grow: ${summary.harvestedCount}"></span></c:if>
							<c:if test="${summary.lostCount > 0}"><span class="status-seg seg-lost" style="flex-grow: ${summary.lostCount}"></span></c:if>
						</div>

						<div class="status-legend">
							<a class="status-link" href="${ctx}/plantings?status=PLANNED">
								<span class="status-count">${summary.plannedCount}</span>
								<span class="status-name name-planned">Planejados</span>
							</a>
							<a class="status-link" href="${ctx}/plantings?status=GROWING">
								<span class="status-count">${summary.growingCount}</span>
								<span class="status-name name-growing">Em desenvolvimento</span>
							</a>
							<a class="status-link" href="${ctx}/plantings?status=HARVESTED">
								<span class="status-count">${summary.harvestedCount}</span>
								<span class="status-name name-harvested">Colhidos</span>
							</a>
							<a class="status-link" href="${ctx}/plantings?status=LOST">
								<span class="status-count">${summary.lostCount}</span>
								<span class="status-name name-lost">Perdidos</span>
							</a>
						</div>
					</c:when>
					<c:otherwise>
						<p class="muted">Nenhum plantio cadastrado ainda. Cadastre o primeiro para acompanhar a safra aqui.</p>
					</c:otherwise>
				</c:choose>

				<dl class="facts">
					<div class="fact">
						<dt>Em campo agora</dt>
						<dd><fmt:formatNumber value="${summary.growingArea}" maxFractionDigits="1" /><span class="fact-unit">ha</span></dd>
						<dd class="fact-note">em ${summary.growingCount} ${summary.growingCount == 1 ? 'plantio' : 'plantios'}</dd>
					</div>
					<div class="fact">
						<dt>Produção estimada</dt>
						<dd><fmt:formatNumber value="${summary.growingBags}" maxFractionDigits="0" /><span class="fact-unit">sacas</span></dd>
						<dd class="fact-note">área x produtividade esperada</dd>
					</div>
					<div class="fact">
						<dt>Colheitas em 30 dias</dt>
						<dd>${summary.harvestSoonCount}</dd>
						<dd class="fact-note">plantios em desenvolvimento</dd>
					</div>
					<div class="fact">
						<dt>Propriedades</dt>
						<dd>${summary.propertyCount}</dd>
						<dd class="fact-note"><fmt:formatNumber value="${summary.propertyArea}" maxFractionDigits="2" /> ha no total</dd>
					</div>
				</dl>

				<c:if test="${summary.overdueCount > 0}">
					<div class="alert-strip" role="status">
						<ui:icon name="triangle-alert" />
						<span>
							${summary.overdueCount} ${summary.overdueCount == 1 ? 'plantio passou' : 'plantios passaram'} da data prevista de colheita e
							${summary.overdueCount == 1 ? 'continua' : 'continuam'} em desenvolvimento.
						</span>
						<a href="${ctx}/plantings?status=GROWING">Revisar a situação</a>
					</div>
				</c:if>
			</section>

			<%-- Calendário da safra --%>
			<div class="section-head">
				<h2>Calendário da safra <c:out value="${calendar.label}" /></h2>
				<p class="muted">De 1º de julho a 30 de junho, uma linha por propriedade. Clique em uma barra para editar o plantio.</p>
			</div>

			<section class="panel season" aria-label="Calendário da safra">
				<c:choose>
					<c:when test="${empty calendar.rows}">
						<p class="panel-empty">Nenhum plantio nesta safra ainda.</p>
					</c:when>
					<c:otherwise>
						<div class="season-scroll">
							<div class="season-board">
								<div class="season-grid" aria-hidden="true">
									<c:forEach var="m" items="${calendar.months}">
										<span class="gridline" style="--l: ${m.leftCss}"></span>
									</c:forEach>
									<span class="today-line" style="--l: ${calendar.todayCss}"></span>
									<span class="today-flag" style="--l: ${calendar.todayCss}">hoje</span>
								</div>

								<div class="season-head">
									<div></div>
									<div class="season-months" aria-hidden="true">
										<c:forEach var="m" items="${calendar.months}">
											<span class="month${m.current ? ' is-current' : ''}" style="--l: ${m.leftCss}; --w: ${m.widthCss}"><span class="month-name"><c:out value="${m.label}" /></span></span>
										</c:forEach>
									</div>
								</div>

								<%-- Uma linha por propriedade; plantios simultâneos na mesma propriedade ficam em faixas --%>
								<c:forEach var="row" items="${calendar.rows}">
									<div class="season-row">
										<div class="season-label">
											<strong><c:out value="${row.property.name}" /></strong>
											<span><c:out value="${row.property.location}" /></span>
										</div>
										<div class="season-track" style="--lanes: ${row.laneCount}">
											<c:forEach var="bar" items="${row.bars}">
												<c:url var="editUrl" value="/planting/update"><c:param name="id" value="${bar.planting.id}" /></c:url>
												<a class="bar bar-${bar.planting.status.cssClass}${bar.startsBefore ? ' cut-start' : ''}${bar.endsAfter ? ' cut-end' : ''}${bar.width < 6 ? ' bar-narrow' : ''}"
													style="--l: ${bar.leftCss}; --w: ${bar.widthCss}; --lane: ${bar.lane}"
													href="<c:out value='${editUrl}'/>"
													title="<c:out value='${bar.planting.crop}'/>, <fmt:formatDate value='${bar.planting.plantingDate}' pattern='dd/MM/yyyy'/> a <fmt:formatDate value='${bar.planting.expectedHarvest}' pattern='dd/MM/yyyy'/> (<c:out value='${bar.planting.status.label}'/>)"
													aria-label="Editar plantio de <c:out value='${bar.planting.crop}'/> em <c:out value='${row.property.name}'/>, <c:out value='${bar.planting.status.label}'/>"><span class="bar-text"><c:out value="${bar.planting.crop}" /></span></a>
											</c:forEach>
										</div>
									</div>
								</c:forEach>
							</div>
						</div>

						<div class="legend">
							<span class="legend-item"><span class="swatch bar-planned"></span>Planejado</span>
							<span class="legend-item"><span class="swatch bar-growing"></span>Em desenvolvimento</span>
							<span class="legend-item"><span class="swatch bar-harvested"></span>Colhido</span>
							<span class="legend-item"><span class="swatch bar-lost"></span>Perdido</span>
						</div>
						<c:if test="${calendar.truncated}">
							<p class="season-note">A safra tem mais plantios do que cabem aqui. <a href="${ctx}/plantings">Veja a lista completa em Plantios</a>.</p>
						</c:if>
					</c:otherwise>
				</c:choose>
			</section>

			<div class="two-col">
				<div>
					<div class="section-head">
						<h2>Próximas colheitas</h2>
						<a href="${ctx}/plantings?status=GROWING">Ver todos os plantios em desenvolvimento</a>
					</div>
					<section class="panel" aria-label="Próximas colheitas">
						<c:choose>
							<c:when test="${empty nextHarvests}">
								<p class="panel-empty">Nenhum plantio em desenvolvimento no momento.</p>
							</c:when>
							<c:otherwise>
								<ul class="harvest-list">
									<c:forEach var="p" items="${nextHarvests}">
										<c:url var="editUrl" value="/planting/update"><c:param name="id" value="${p.id}" /></c:url>
										<li>
											<a class="harvest-item" href="<c:out value='${editUrl}'/>">
												<span>
													<span class="cell-title"><c:out value="${p.crop}" /><c:if test="${not empty p.variety}"> <span class="variety"><c:out value="${p.variety}" /></span></c:if></span>
													<span class="cell-sub"><c:out value="${p.property.name}" />, <c:out value="${p.property.location}" /></span>
												</span>
												<span class="harvest-when${p.daysToHarvest < 0 ? ' late' : ''}">
													<fmt:formatDate value="${p.expectedHarvest}" pattern="dd/MM/yyyy" />
													<small><c:out value="${p.harvestCountdown}" /></small>
												</span>
											</a>
										</li>
									</c:forEach>
								</ul>
							</c:otherwise>
						</c:choose>
					</section>
				</div>

				<div>
					<div class="section-head">
						<h2>Área por cultura</h2>
						<p class="muted">Planejado e em desenvolvimento</p>
					</div>
					<section class="panel" aria-label="Área por cultura">
						<c:choose>
							<c:when test="${empty crops}">
								<p class="panel-empty">Sem plantios em campo.</p>
							</c:when>
							<c:otherwise>
								<ul class="crop-list">
									<c:forEach var="crop" items="${crops}">
										<li class="crop-row">
											<div class="crop-line">
												<span><c:out value="${crop.crop}" /></span>
												<small><fmt:formatNumber value="${crop.area}" maxFractionDigits="1" /> ha em ${crop.plantingCount} ${crop.plantingCount == 1 ? 'plantio' : 'plantios'}</small>
											</div>
											<div class="crop-track"><div class="crop-fill" style="--p: ${crop.percentCss}"></div></div>
										</li>
									</c:forEach>
								</ul>
							</c:otherwise>
						</c:choose>
					</section>
				</div>
			</div>
		</c:if>
	</main>
</body>
</html>
