package controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ModelException;
import model.PasswordUtil;
import model.User;
import model.dao.DAOFactory;
import model.dao.UserDAO;

/**
 * Controller do login.
 *
 *   GET  /login   mostra a tela de login (se já estiver logado, vai para o início)
 *   POST /login   confere e-mail e senha; se estiverem certos, guarda o usuário na sessão
 *   POST /logout  encerra a sessão (é POST de propósito: sair altera o estado da sessão e
 *                 assim passa pela verificação CSRF do AuthenticationFilter)
 *
 * A senha nunca é guardada nem comparada em texto puro: no banco fica um hash PBKDF2
 * (ver PasswordUtil) e a verificação refaz o cálculo com o salt gravado junto.
 */
@WebServlet(urlPatterns = { "/login", "/logout" })
public class LoginController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// Mesma mensagem para "e-mail não existe" e "senha errada": quem tenta adivinhar
	// não descobre quais e-mails estão cadastrados.
	private static final String INVALID_CREDENTIALS = "E-mail ou senha incorretos.";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (req.getServletPath().equals("/logout")) {
			// sair é POST; quem digitar /logout na barra de endereço só volta para o início
			ControllerUtil.redirect(resp, req.getContextPath() + "/");
			return;
		}

		if (AuthenticationFilter.loggedUser(req) != null) {
			ControllerUtil.redirect(resp, req.getContextPath() + "/");
			return;
		}

		// mensagens que chegam pela URL (depois de sair ou quando a sessão expira)
		if (req.getParameter("out") != null)
			req.setAttribute("info", "Você saiu da sua conta.");
		else if (req.getParameter("expired") != null)
			req.setAttribute("loginError", "Sua sessão expirou. Entre novamente.");

		showLogin(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (req.getServletPath().equals("/logout")) {
			logout(req, resp);
			return;
		}

		login(req, resp);
	}

	private void showLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// a sessão existe a partir daqui: é nela que fica o token CSRF do formulário de login
		AuthenticationFilter.csrfToken(req.getSession());

		ControllerUtil.forward(req, resp, "/login.jsp");
	}

	private void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String email = ControllerUtil.trim(req.getParameter("email"));
		String password = req.getParameter("password");

		req.setAttribute("email", email);

		if (email.isEmpty() || password == null || password.isEmpty()) {
			req.setAttribute("loginError", "Informe seu e-mail e sua senha.");
			showLogin(req, resp);
			return;
		}

		// limite de tamanho: evita gastar processamento calculando hash de textos gigantes
		if (email.length() > 150 || password.length() > 200) {
			req.setAttribute("loginError", INVALID_CREDENTIALS);
			showLogin(req, resp);
			return;
		}

		UserDAO dao = DAOFactory.createDAO(UserDAO.class);

		User user;
		try {
			user = dao.findByEmail(email);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			req.setAttribute("loginError", "Não foi possível consultar o banco de dados agora. Tente novamente.");
			showLogin(req, resp);
			return;
		}

		boolean valid;
		if (user == null || user.getPasswordHash() == null) {
			// Gasta o mesmo tempo de uma verificação real: pelo tempo de resposta,
			// ninguém consegue saber se o e-mail existe.
			PasswordUtil.burnTime(password);
			valid = false;
		} else {
			valid = PasswordUtil.matches(password, user.getPasswordHash());
		}

		if (!valid) {
			req.setAttribute("loginError", INVALID_CREDENTIALS);
			showLogin(req, resp);
			return;
		}

		// ---- login correto ----
		HttpSession session = req.getSession();

		// Troca o id da sessão (proteção contra "session fixation": se alguém tivesse
		// plantado um id antigo no navegador da vítima, ele deixa de valer agora).
		req.changeSessionId();

		// Guardamos uma cópia SEM o hash da senha
		User logged = new User(user.getId());
		logged.setName(user.getName());
		logged.setGender(user.getGender());
		logged.setEmail(user.getEmail());

		session.setAttribute(AuthenticationFilter.USER_KEY, logged);
		AuthenticationFilter.renewCsrfToken(session);

		// vai para a página que a pessoa tentou abrir antes do login (se houver)
		String target = (String) session.getAttribute(AuthenticationFilter.AFTER_LOGIN_KEY);
		session.removeAttribute(AuthenticationFilter.AFTER_LOGIN_KEY);

		String context = req.getContextPath();

		if (target == null || !target.startsWith(context + "/") || target.startsWith(context + "/login")
				|| target.startsWith(context + "/logout"))
			target = context + "/";

		ControllerUtil.redirect(resp, target);
	}

	private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession(false);

		if (session != null)
			session.invalidate();

		ControllerUtil.redirect(resp, req.getContextPath() + "/login?out=1");
	}
}
