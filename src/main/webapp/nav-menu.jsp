<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%-- Cabeçalho com o menu de todos os cadastros. A página define antes "activeMenu"
     para o item atual ficar destacado. Os dois cadastros novos (Propriedades e Plantios)
     estão logo depois de Início. --%>
<a class="skip-link" href="#conteudo">Ir para o conteúdo</a>
<header class="topbar">
	<div class="topbar-inner">
		<a class="brand" href="${ctx}/">
			<span class="brand-mark"><ui:icon name="sprout"/></span>
			CRUD Manager
		</a>

		<nav class="nav" aria-label="Principal">
			<a class="nav-link" href="${ctx}/" ${activeMenu eq 'home' ? 'aria-current="page"' : ''}><ui:icon name="layout-dashboard"/>Início</a>
			<a class="nav-link" href="${ctx}/properties" ${activeMenu eq 'properties' ? 'aria-current="page"' : ''}><ui:icon name="land-plot"/>Propriedades</a>
			<a class="nav-link" href="${ctx}/plantings" ${activeMenu eq 'plantings' ? 'aria-current="page"' : ''}><ui:icon name="sprout"/>Plantios</a>
			<a class="nav-link" href="${ctx}/users" ${activeMenu eq 'users' ? 'aria-current="page"' : ''}><ui:icon name="users"/>Usuários</a>
			<a class="nav-link" href="${ctx}/posts" ${activeMenu eq 'posts' ? 'aria-current="page"' : ''}><ui:icon name="message-square-text"/>Posts</a>
			<a class="nav-link" href="${ctx}/companies" ${activeMenu eq 'companies' ? 'aria-current="page"' : ''}><ui:icon name="building-2"/>Empresas</a>
		</nav>

		<div class="user-menu">
			<span class="avatar" aria-hidden="true"><c:out value="${loggedUser.initials}"/></span>
			<span class="user-name"><c:out value="${loggedUser.firstName}"/></span>
			<form method="post" action="${ctx}/logout">
				<%@ include file="/WEB-INF/fragments/csrf.jspf" %>
				<button class="btn-leave" type="submit"><ui:icon name="log-out"/>Sair</button>
			</form>
		</div>
	</div>
</header>
