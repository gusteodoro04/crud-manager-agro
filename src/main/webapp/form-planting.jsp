<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="isUpdate" value="${action eq 'update'}" />
<c:set var="pageTitle" value="${isUpdate ? 'Editar plantio' : 'Novo plantio'}" />
<c:set var="activeMenu" value="plantings" />
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
				<h1 class="page-title">${isUpdate ? 'Editar plantio' : 'Novo plantio'}</h1>
				<p class="page-sub">${isUpdate ? 'Atualize os dados e salve.' : 'Informe o que foi (ou será) plantado e onde.'}</p>
			</div>
		</div>

		<%-- Os campos obrigatórios têm "required"; o navegador avisa antes de enviar e o servidor
		     valida de novo (nunca confie só no navegador). --%>
		<form class="panel form-panel" method="post" action="${ctx}/planting/${action}">
			<%@ include file="/WEB-INF/fragments/csrf.jspf"%>
			<c:if test="${isUpdate}"><input type="hidden" name="id" value="<c:out value='${form.id}'/>"></c:if>
			<input type="hidden" name="back" value="<c:out value='${back}'/>">

			<c:if test="${not empty errors}">
				<div class="form-error" role="alert">
					<ui:icon name="circle-alert" />
					<span>Revise ${fn:length(errors) == 1 ? 'o campo marcado' : 'os campos marcados'} abaixo.</span>
				</div>
			</c:if>

			<div class="form-section">
				<h2 class="section-title">O plantio</h2>
				<p class="section-sub">Cultura, variedade e a situação em que ele está.</p>

				<ui:field name="crop" label="Cultura" hint="Ex.: Café, Soja, Milho.">
					<%-- texto com sugestões (datalist): digita livre ou escolhe uma sugestão --%>
					<input class="input" type="text" id="crop" name="crop" list="crop-options" maxlength="80" required
						autocomplete="off" value="<c:out value='${form.crop}'/>"
						aria-invalid="${not empty errors.crop}" aria-describedby="crop-hint crop-error">
					<datalist id="crop-options">
						<option value="Café"></option>
						<option value="Soja"></option>
						<option value="Milho"></option>
						<option value="Feijão"></option>
						<option value="Cana-de-açúcar"></option>
						<option value="Trigo"></option>
						<option value="Arroz"></option>
						<option value="Algodão"></option>
						<option value="Mandioca"></option>
						<option value="Tomate"></option>
						<option value="Batata"></option>
						<option value="Eucalipto"></option>
					</datalist>
				</ui:field>

				<ui:field name="variety" label="Variedade" hint="Cultivar ou híbrido, se houver." optional="true">
					<input class="input" type="text" id="variety" name="variety" maxlength="80"
						value="<c:out value='${form.variety}'/>"
						aria-invalid="${not empty errors.variety}" aria-describedby="variety-hint variety-error">
				</ui:field>

				<ui:field name="status" label="Situação" group="true">
					<div class="segmented" role="radiogroup" aria-labelledby="status-label">
						<c:forEach var="s" items="${statuses}">
							<label class="seg">
								<input type="radio" name="status" value="${s.name()}" ${form.status eq s.name() ? 'checked' : ''} required>
								<span class="seg-body"><c:out value="${s.label}" /></span>
							</label>
						</c:forEach>
					</div>
				</ui:field>
			</div>

			<div class="form-section">
				<h2 class="section-title">Onde e quem cuida</h2>
				<p class="section-sub">A propriedade e o responsável já precisam estar cadastrados.</p>

				<ui:field name="property" label="Propriedade" hint="Em qual propriedade o plantio está.">
					<select class="select" id="property" name="property" required data-area-source
						aria-invalid="${not empty errors.property}" aria-describedby="property-hint property-error">
						<option value="" disabled ${empty form.property ? 'selected' : ''}>Selecione a propriedade</option>
						<c:forEach var="pr" items="${properties}">
							<option value="${pr.id}" data-area="${pr.area}" data-area-label="<fmt:formatNumber value='${pr.area}' maxFractionDigits='2'/> ha"
								${form.property eq pr.id.toString() ? 'selected' : ''}><c:out value="${pr.name}" /> (<c:out value="${pr.location}" />)</option>
						</c:forEach>
					</select>
					<c:if test="${empty properties}">
						<p class="field-hint">Nenhuma propriedade cadastrada ainda. <a href="${ctx}/property/form">Cadastre uma propriedade</a> primeiro.</p>
					</c:if>
				</ui:field>

				<ui:field name="manager" label="Responsável" hint="Quem acompanha este plantio.">
					<select class="select" id="manager" name="manager" required
						aria-invalid="${not empty errors.manager}" aria-describedby="manager-hint manager-error">
						<option value="" disabled ${empty form.manager ? 'selected' : ''}>Selecione o responsável</option>
						<c:forEach var="u" items="${users}">
							<option value="${u.id}" ${form.manager eq u.id.toString() ? 'selected' : ''}><c:out value="${u.name}" /></option>
						</c:forEach>
					</select>
				</ui:field>
			</div>

			<div class="form-section">
				<h2 class="section-title">Datas e produção</h2>
				<p class="section-sub">A colheita não pode ser antes do plantio, e a área não pode passar da propriedade.</p>

				<ui:field name="plantingDate" label="Data do plantio">
					<input class="input input-short" type="date" id="plantingDate" name="plantingDate" required data-min-source
						value="<c:out value='${form.plantingDate}'/>"
						aria-invalid="${not empty errors.plantingDate}" aria-describedby="plantingDate-error">
				</ui:field>

				<ui:field name="expectedHarvest" label="Colheita prevista">
					<input class="input input-short" type="date" id="expectedHarvest" name="expectedHarvest" required data-min-target
						value="<c:out value='${form.expectedHarvest}'/>"
						aria-invalid="${not empty errors.expectedHarvest}" aria-describedby="expectedHarvest-error">
				</ui:field>

				<ui:field name="area" label="Área plantada" hint="Em hectares (ha).">
					<div class="input-unit">
						<input class="input" type="number" id="area" name="area" min="0.01" step="0.01" inputmode="decimal" required data-area-target
							value="<c:out value='${form.area}'/>"
							aria-invalid="${not empty errors.area}" aria-describedby="area-hint area-error area-note">
						<span class="unit">ha</span>
					</div>
					<p class="field-hint" id="area-note" data-area-note></p>
				</ui:field>

				<ui:field name="expectedYield" label="Produtividade esperada" hint="Sacas colhidas por hectare. Usada para estimar a produção.">
					<div class="input-unit">
						<input class="input" type="number" id="expectedYield" name="expectedYield" min="0" step="0.1" inputmode="decimal" required
							value="<c:out value='${form.expectedYield}'/>"
							aria-invalid="${not empty errors.expectedYield}" aria-describedby="expectedYield-hint expectedYield-error">
						<span class="unit">sc/ha</span>
					</div>
				</ui:field>
			</div>

			<div class="form-section">
				<h2 class="section-title">Observações</h2>

				<ui:field name="notes" label="Anotações" hint="Adubação, pragas, clima, o que for útil lembrar." optional="true">
					<textarea class="textarea" id="notes" name="notes" rows="4" maxlength="2000"
						aria-invalid="${not empty errors.notes}" aria-describedby="notes-hint notes-error"><c:out value="${form.notes}" /></textarea>
				</ui:field>
			</div>

			<div class="form-actions">
				<a class="btn btn-secondary" href="${ctx}/plantings${empty back ? '' : '?'}<c:out value='${back}'/>">Cancelar</a>
				<button class="btn btn-primary" type="submit"><ui:icon name="check" />${isUpdate ? 'Salvar alterações' : 'Cadastrar plantio'}</button>
			</div>
		</form>
	</main>
</body>
</html>
