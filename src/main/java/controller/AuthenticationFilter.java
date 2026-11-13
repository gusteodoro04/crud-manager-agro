package controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * Filtro de login: passa por ele TODA requisição do sistema (urlPatterns = "/*") antes de
 * chegar a qualquer Servlet. Ele faz quatro coisas, nesta ordem:
 *
 *  1. Configura UTF-8 (substitui o antigo CharacterEncodingFilter; precisa vir antes de
 *     qualquer req.getParameter(), senão os acentos do formulário chegam errados).
 *  2. Bloqueia o acesso direto a arquivos .jsp (as telas só abrem através dos controllers).
 *  3. Se a pessoa não está logada, manda para /login (guardando a página que ela queria
 *     abrir, para voltar depois do login).
 *  4. Em todo POST, confere o token CSRF: um código secreto da sessão que vai escondido em
 *     cada formulário. Sem ele, outro site não consegue enviar formulários "em nome" do
 *     usuário logado.
 *
 * O usuário logado fica na sessão com o nome "loggedUser" (ver LoginController).
 */
@WebFilter(urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

	public static final String USER_KEY = "loggedUser";
	public static final String CSRF_KEY = "csrfToken";
	public static final String AFTER_LOGIN_KEY = "afterLogin";

	private static final SecureRandom RANDOM = new SecureRandom();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		// 1) UTF-8
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		// getServletPath() já vem sem o contexto (/crud-manager), sem ";jsessionid" e sem "?query"
		String path = req.getServletPath();

		boolean isStatic = isStaticResource(path);

		// Páginas com dados da pessoa não devem ficar guardadas no cache do navegador:
		// senão, depois de "Sair", o botão Voltar mostraria a tela anterior.
		if (!isStatic)
			resp.setHeader("Cache-Control", "no-store");

		// 2) JSP direto na URL: não existe (as telas só abrem pelos controllers)
		if (path.endsWith(".jsp") || path.endsWith(".jspf")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Arquivos de estilo, script, fontes e imagens e a própria tela de login são públicos
		boolean isPublic = isStatic || path.equals("/login");

		HttpSession session = req.getSession(false);
		boolean logged = session != null && session.getAttribute(USER_KEY) != null;

		// 3) exige login
		if (!isPublic && !logged) {
			// guarda a página pedida para abrir depois do login (só telas: nada de
			// /favicon.ico e similares, que o navegador pede sozinho)
			if (isGet(req) && !path.contains("."))
				req.getSession().setAttribute(AFTER_LOGIN_KEY, requestedUrl(req));

			resp.sendRedirect(req.getContextPath() + "/login");
			return;
		}

		// 4) CSRF: qualquer requisição que altera dados (tudo que não é GET/HEAD/OPTIONS)
		if (!isSafeMethod(req) && !hasValidCsrfToken(req, session)) {
			if (logged)
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			else
				resp.sendRedirect(req.getContextPath() + "/login?expired=1");
			return;
		}

		chain.doFilter(request, response);
	}

	// ------------------------------------------------------------------
	// Token CSRF
	// ------------------------------------------------------------------

	/** Devolve o token CSRF da sessão, criando um novo se ainda não existir. */
	public static String csrfToken(HttpSession session) {
		String token = (String) session.getAttribute(CSRF_KEY);

		if (token == null) {
			token = newToken();
			session.setAttribute(CSRF_KEY, token);
		}

		return token;
	}

	/** Troca o token (feito no login, junto com a troca do id da sessão). */
	public static String renewCsrfToken(HttpSession session) {
		String token = newToken();
		session.setAttribute(CSRF_KEY, token);
		return token;
	}

	private static String newToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private boolean hasValidCsrfToken(HttpServletRequest req, HttpSession session) {
		if (session == null)
			return false;

		String expected = (String) session.getAttribute(CSRF_KEY);
		String received = req.getParameter("_csrf");

		if (expected == null || received == null)
			return false;

		// comparação em tempo constante (não vaza, pelo tempo, quantos caracteres acertou)
		return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
				received.getBytes(StandardCharsets.UTF_8));
	}

	// ------------------------------------------------------------------
	// Apoio
	// ------------------------------------------------------------------

	private boolean isStaticResource(String path) {
		return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/fonts/")
				|| path.startsWith("/images/");
	}

	private boolean isGet(HttpServletRequest req) {
		return "GET".equalsIgnoreCase(req.getMethod());
	}

	private boolean isSafeMethod(HttpServletRequest req) {
		String method = req.getMethod();
		return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
				|| "OPTIONS".equalsIgnoreCase(method);
	}

	private String requestedUrl(HttpServletRequest req) {
		String query = req.getQueryString();
		return req.getRequestURI() + (query == null ? "" : "?" + query);
	}

	/** Usuário logado (ou null). */
	public static User loggedUser(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		return session == null ? null : (User) session.getAttribute(USER_KEY);
	}
}
