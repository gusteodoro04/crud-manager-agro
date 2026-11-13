package controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ModelException;
import model.PasswordUtil;
import model.User;
import model.dao.DAOFactory;
import model.dao.UserDAO;

/**
 * CRUD de Usuários. Na atividade 3 ganhou: senha (gravada como hash), lista paginada com
 * busca, validação dos campos e proteção para ninguém excluir a si mesmo.
 *
 * Em vez de comparar getRequestURI() com "/crud-manager/user/form" (o que só funcionava se
 * o projeto estivesse publicado com esse nome), usamos getServletPath(), que já vem sem o
 * nome do contexto: o sistema funciona com qualquer nome de projeto no Tomcat.
 */
@WebServlet(urlPatterns = { "/users", "/user/form", "/user/delete", "/user/insert", "/user/update" })
public class UsersController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String LIST_PATH = "/users";
	private static final String FORM_PAGE = "/form-user.jsp";

	private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/user/form":
			showForm(req, resp, "insert", new LinkedHashMap<String, String>(), new LinkedHashMap<String, String>());
			break;
		case "/user/update": {
			User user = findUser(req.getParameter("id"));

			if (user == null) {
				ControllerUtil.errorMessage(req, "Usuário não encontrado.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}

			Map<String, String> form = new LinkedHashMap<String, String>();
			form.put("id", String.valueOf(user.getId()));
			form.put("name", user.getName());
			form.put("gender", user.getGender() == null ? "" : user.getGender());
			form.put("mail", user.getEmail());

			showForm(req, resp, "update", form, new LinkedHashMap<String, String>());
			break;
		}
		default:
			list(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/user/insert":
			save(req, resp, false);
			break;
		case "/user/update":
			save(req, resp, true);
			break;
		case "/user/delete":
			delete(req);
			ControllerUtil.redirectToList(req, resp, LIST_PATH);
			break;
		default:
			ControllerUtil.redirect(resp, req.getContextPath() + LIST_PATH);
		}
	}

	// ------------------------------------------------------------------
	// Lista com paginação e busca
	// ------------------------------------------------------------------

	private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String search = ControllerUtil.searchTerm(req);

		UserDAO dao = DAOFactory.createDAO(UserDAO.class);

		try {
			int total = dao.count(search);

			Pagination pagination = Pagination.from(req, LIST_PATH, total).param("q", search);

			List<User> users = dao.listPage(search, pagination.getPageSize(), pagination.getOffset());

			req.setAttribute("users", users);
			req.setAttribute("pagination", pagination);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			ControllerUtil.errorMessageNow(req, "Não foi possível carregar os usuários agora.");
		}

		req.setAttribute("search", search);

		ControllerUtil.transferSessionMessagesToRequest(req);

		ControllerUtil.forward(req, resp, "/users.jsp");
	}

	// ------------------------------------------------------------------
	// Formulário
	// ------------------------------------------------------------------

	private void showForm(HttpServletRequest req, HttpServletResponse resp, String action,
			Map<String, String> form, Map<String, String> errors) throws ServletException, IOException {

		req.setAttribute("action", action);
		req.setAttribute("form", form);
		req.setAttribute("errors", errors);
		req.setAttribute("back", ControllerUtil.backQuery(req));

		ControllerUtil.forward(req, resp, FORM_PAGE);
	}

	private Map<String, String> readForm(HttpServletRequest req) {
		Map<String, String> form = new LinkedHashMap<String, String>();

		form.put("id", ControllerUtil.trim(req.getParameter("id")));
		form.put("name", ControllerUtil.trim(req.getParameter("name")));
		form.put("gender", ControllerUtil.trim(req.getParameter("gender")));
		form.put("mail", ControllerUtil.trim(req.getParameter("mail")));

		return form;
	}

	// ------------------------------------------------------------------
	// Inserir / atualizar
	// ------------------------------------------------------------------

	private void save(HttpServletRequest req, HttpServletResponse resp, boolean isUpdate)
			throws ServletException, IOException {

		Map<String, String> form = readForm(req);
		Map<String, String> errors = new LinkedHashMap<String, String>();

		// a senha NÃO volta para o formulário em caso de erro (nem fica no mapa "form")
		String password = req.getParameter("password");

		User user = new User();

		if (isUpdate) {
			user = findUser(form.get("id"));

			if (user == null) {
				ControllerUtil.errorMessage(req, "Usuário não encontrado.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}
		}

		validate(form, password, isUpdate, user, errors);

		if (!errors.isEmpty()) {
			showForm(req, resp, isUpdate ? "update" : "insert", form, errors);
			return;
		}

		UserDAO dao = DAOFactory.createDAO(UserDAO.class);

		try {
			boolean ok = isUpdate ? dao.update(user) : dao.save(user);

			if (ok) {
				ControllerUtil.sucessMessage(req, "Usuário '" + user.getName() + "' "
						+ (isUpdate ? "atualizado" : "salvo") + " com sucesso.");

				// se a pessoa editou o próprio cadastro, atualiza o nome mostrado no topo
				User logged = AuthenticationFilter.loggedUser(req);
				if (isUpdate && logged != null && logged.getId() == user.getId()) {
					User fresh = new User(user.getId());
					fresh.setName(user.getName());
					fresh.setGender(user.getGender());
					fresh.setEmail(user.getEmail());
					req.getSession().setAttribute(AuthenticationFilter.USER_KEY, fresh);
				}
			} else {
				ControllerUtil.errorMessage(req, "Usuário '" + user.getName() + "' não pode ser "
						+ (isUpdate ? "atualizado" : "salvo") + ".");
			}
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();

			ControllerUtil.errorMessageNow(req, e.getMessage());
			showForm(req, resp, isUpdate ? "update" : "insert", form, errors);
			return;
		}

		ControllerUtil.redirectToList(req, resp, LIST_PATH);
	}

	private void validate(Map<String, String> form, String password, boolean isUpdate, User user,
			Map<String, String> errors) {

		// nome
		String name = form.get("name");
		if (name.isEmpty())
			errors.put("name", "Informe o nome.");
		else if (name.length() > 150)
			errors.put("name", "Use no máximo 150 caracteres.");
		user.setName(name);

		// sexo (M ou F)
		String gender = form.get("gender");
		if (!gender.equals("M") && !gender.equals("F"))
			errors.put("gender", "Escolha uma opção.");
		else
			user.setGender(gender);

		// e-mail: formato e unicidade (o login usa o e-mail, então não pode repetir)
		String mail = form.get("mail");
		if (mail.isEmpty()) {
			errors.put("mail", "Informe o e-mail.");
		} else if (mail.length() > 150 || !EMAIL.matcher(mail).matches()) {
			errors.put("mail", "Informe um e-mail válido, como nome@empresa.com.");
		} else {
			User sameMail = findByEmail(mail);

			if (sameMail != null && (!isUpdate || sameMail.getId() != user.getId()))
				errors.put("mail", "Este e-mail já está cadastrado.");
		}
		user.setEmail(mail);

		// senha: obrigatória ao cadastrar; ao editar, vazia = manter a atual
		boolean passwordEmpty = password == null || password.isEmpty();

		if (passwordEmpty && !isUpdate) {
			errors.put("password", "Defina uma senha.");
		} else if (!passwordEmpty && password.length() < 6) {
			errors.put("password", "Use pelo menos 6 caracteres.");
		} else if (!passwordEmpty && password.length() > 100) {
			errors.put("password", "Use no máximo 100 caracteres.");
		} else if (!passwordEmpty) {
			user.setPasswordHash(PasswordUtil.hash(password));
		} else {
			user.setPasswordHash(null); // null = o DAO não mexe na senha
		}
	}

	// ------------------------------------------------------------------
	// Excluir
	// ------------------------------------------------------------------

	private void delete(HttpServletRequest req) {
		User user = findUser(req.getParameter("id"));

		if (user == null) {
			ControllerUtil.errorMessage(req, "Usuário não encontrado para exclusão.");
			return;
		}

		User logged = AuthenticationFilter.loggedUser(req);
		if (logged != null && logged.getId() == user.getId()) {
			ControllerUtil.errorMessage(req, "Você não pode excluir o usuário com o qual está conectado.");
			return;
		}

		UserDAO dao = DAOFactory.createDAO(UserDAO.class);

		try {
			if (dao.delete(user)) {
				ControllerUtil.sucessMessage(req, "Usuário '" + user.getName() + "' excluído com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Usuário '" + user.getName()
						+ "' não pode ser excluído: há posts, empresas, propriedades ou plantios ligados a ele.");
			}
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			ControllerUtil.errorMessage(req, e.getMessage());
		}
	}

	// ------------------------------------------------------------------
	// Buscas (devolvem null se não achar)
	// ------------------------------------------------------------------

	private User findUser(String idParameter) {
		Integer id = ControllerUtil.parseInteger(idParameter);

		if (id == null)
			return null;

		try {
			UserDAO dao = DAOFactory.createDAO(UserDAO.class);
			return dao.findById(id);
		} catch (ModelException e) {
			e.printStackTrace();
			return null;
		}
	}

	private User findByEmail(String email) {
		try {
			UserDAO dao = DAOFactory.createDAO(UserDAO.class);
			return dao.findByEmail(email);
		} catch (ModelException e) {
			e.printStackTrace();
			return null;
		}
	}
}
