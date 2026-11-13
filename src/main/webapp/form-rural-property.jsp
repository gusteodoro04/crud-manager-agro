<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags"%>
<c:set var="isUpdate" value="${action eq 'update'}" />
<c:set var="pageTitle" value="${isUpdate ? 'Editar propriedade' : 'Nova propriedade'}" />
<c:set var="activeMenu" value="properties" />
<jsp:useBean id="today" class="java.util.Date" />
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
				<h1 class="page-title">${isUpdate ? 'Editar propriedade' : 'Nova propriedade'}</h1>
				<p class="page-sub">${isUpdate ? 'Atualize os dados e salve.' : 'Cadastre a propriedade para poder registrar plantios nela.'}</p>
			</div>
		</div>

		<form class="panel form-panel" method="post" action="${ctx}/property/${action}">
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
				<h2 class="section-title">Identificação</h2>
				<p class="section-sub">Como a propriedade é chamada e de que tipo ela é.</p>

				<ui:field name="name" label="Nome" hint="Ex.: Fazenda Boa Esperança.">
					<input class="input" type="text" id="name" name="name" maxlength="120" required
						value="<c:out value='${form.name}'/>"
						aria-invalid="${not empty errors.name}" aria-describedby="name-hint name-error">
				</ui:field>

				<ui:field name="type" label="Tipo" group="true">
					<div class="tiles" role="radiogroup" aria-labelledby="type-label">
						<c:forEach var="t" items="${types}">
							<label class="tile">
								<input type="radio" name="type" value="${t.name()}" ${form.type eq t.name() ? 'checked' : ''} required>
								<span class="tile-body">
									<span class="tile-title"><c:out value="${t.label}" /></span>
									<span class="tile-hint"><c:out value="${t.hint}" /></span>
								</span>
							</label>
						</c:forEach>
					</div>
				</ui:field>

				<ui:field name="owner" label="Dono" hint="Usuário responsável pela propriedade.">
					<select class="select" id="owner" name="owner" required
						aria-invalid="${not empty errors.owner}" aria-describedby="owner-hint owner-error">
						<option value="" disabled ${empty form.owner ? 'selected' : ''}>Selecione o dono</option>
						<c:forEach var="u" items="${users}">
							<option value="${u.id}" ${form.owner eq u.id.toString() ? 'selected' : ''}><c:out value="${u.name}" /></option>
						</c:forEach>
					</select>
				</ui:field>
			</div>

			<div class="form-section">
				<h2 class="section-title">Localização</h2>

				<ui:field name="city" label="Cidade">
					<input class="input" type="text" id="city" name="city" maxlength="100" required autocomplete="address-level2"
						value="<c:out value='${form.city}'/>"
						aria-invalid="${not empty errors.city}" aria-describedby="city-error">
				</ui:field>

				<ui:field name="state" label="Estado">
					<select class="select" id="state" name="state" required
						aria-invalid="${not empty errors.state}" aria-describedby="state-error">
						<option value="" disabled ${empty form.state ? 'selected' : ''}>Selecione o estado</option>
						<c:forEach var="uf" items="${states}">
							<option value="${uf.key}" ${form.state eq uf.key ? 'selected' : ''}><c:out value="${uf.value}" /> (${uf.key})</option>
						</c:forEach>
					</select>
				</ui:field>

				<ui:field name="mapUrl" label="Link do mapa" hint="Endereço do Google Maps ou similar, começando com https://." optional="true">
					<input class="input" type="url" id="mapUrl" name="mapUrl" maxlength="500" placeholder="https://maps.google.com/..."
						value="<c:out value='${form.mapUrl}'/>"
						aria-invalid="${not empty errors.mapUrl}" aria-describedby="mapUrl-hint mapUrl-error">
				</ui:field>
			</div>

			<div class="form-section">
				<h2 class="section-title">Terra e manejo</h2>

				<ui:field name="area" label="Área total" hint="Em hectares (1 ha = 10.000 m²).">
					<div class="input-unit">
						<input class="input" type="number" id="area" name="area" min="0.01" step="0.01" inputmode="decimal" required
							value="<c:out value='${form.area}'/>"
							aria-invalid="${not empty errors.area}" aria-describedby="area-hint area-error">
						<span class="unit">ha</span>
					</div>
				</ui:field>

				<ui:field name="acquiredOn" label="Data de aquisição" hint="Quando a propriedade passou a ser sua." optional="true">
					<input class="input input-short" type="date" id="acquiredOn" name="acquiredOn" max="<fmt:formatDate value='${today}' pattern='yyyy-MM-dd'/>"
						value="<c:out value='${form.acquiredOn}'/>"
						aria-invalid="${not empty errors.acquiredOn}" aria-describedby="acquiredOn-hint acquiredOn-error">
				</ui:field>

				<ui:field name="irrigated" label="Irrigação" group="true">
					<label class="switch">
						<input type="checkbox" id="irrigated" name="irrigated" ${form.irrigated eq 'on' ? 'checked' : ''}>
						<span class="switch-track" aria-hidden="true"></span>
						<span>Tem sistema de irrigação</span>
					</label>
				</ui:field>
			</div>

			<div class="form-section">
				<h2 class="section-title">Observações</h2>

				<ui:field name="notes" label="Anotações" hint="Acesso, solo, divisas, o que for útil lembrar." optional="true">
					<textarea class="textarea" id="notes" name="notes" rows="4" maxlength="2000"
						aria-invalid="${not empty errors.notes}" aria-describedby="notes-hint notes-error"><c:out value="${form.notes}" /></textarea>
				</ui:field>
			</div>

			<div class="form-actions">
				<a class="btn btn-secondary" href="${ctx}/properties${empty back ? '' : '?'}<c:out value='${back}'/>">Cancelar</a>
				<button class="btn btn-primary" type="submit"><ui:icon name="check" />${isUpdate ? 'Salvar alterações' : 'Cadastrar propriedade'}</button>
			</div>
		</form>
	</main>
</body>
</html>
