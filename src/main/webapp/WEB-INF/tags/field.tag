<%@ tag body-content="scriptless" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- Uma linha de formulário: rótulo + dica à esquerda, o campo (corpo da tag) e a mensagem
     de erro à direita. O erro vem do mapa "errors", que o controller preenche na validação. --%>
<%@ attribute name="name" required="true" description="nome do campo (chave em errors)" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="hint" required="false" %>
<%@ attribute name="optional" required="false" type="java.lang.Boolean" description="mostra 'opcional' ao lado do rótulo" %>
<%@ attribute name="group" required="false" type="java.lang.Boolean" description="true quando o campo é um grupo de opções (radio): o rótulo não aponta para um input só" %>
<c:set var="error" value="${requestScope.errors[name]}"/>
<div class="field${empty error ? '' : ' has-error'}">
	<div class="field-meta">
		<c:choose>
			<c:when test="${group}">
				<span class="field-label" id="${name}-label"><c:out value="${label}"/></span>
			</c:when>
			<c:otherwise>
				<label class="field-label" for="${name}"><c:out value="${label}"/><c:if test="${optional}"><span class="field-optional">opcional</span></c:if></label>
			</c:otherwise>
		</c:choose>
		<c:if test="${not empty hint}"><p class="field-hint" id="${name}-hint"><c:out value="${hint}"/></p></c:if>
	</div>
	<div class="field-control">
		<jsp:doBody/>
		<c:if test="${not empty error}"><p class="field-error" id="${name}-error"><ui:icon name="circle-alert"/><span><c:out value="${error}"/></span></p></c:if>
	</div>
</div>
