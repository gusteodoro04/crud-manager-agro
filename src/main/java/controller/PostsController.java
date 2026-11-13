package controller;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ModelException;
import model.Post;
import model.User;
import model.dao.DAOFactory;
import model.dao.PostDAO;
import model.dao.UserDAO;

@SuppressWarnings("serial")
// Herda da classe HttpServlet (extends HttpServlet) para ser tratada como um Servlet
// Anota a classe (@WebServlet) ajustando as URLs (urlPatterns) as quais ela responde
//
// Atividade 3: a raiz do sistema ("") agora é o painel inicial (HomeController); a lista de
// posts passou a ficar só em /posts. Também ganhou paginação, busca e validação.
@WebServlet(urlPatterns = {"/posts", "/post/form", "/post/delete", "/post/insert", "/post/update"})
public class PostsController extends HttpServlet {

	private static final String LIST_PATH = "/posts";
	private static final String FORM_PAGE = "/form-post.jsp";

	// Sobrescreve o método doGet, sendo capaz de responder métodos HTTP GET
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/post/form": {
			Map<String, String> form = new LinkedHashMap<String, String>();

			// o autor começa como quem está logado
			User logged = AuthenticationFilter.loggedUser(req);
			if (logged != null)
				form.put("user", String.valueOf(logged.getId()));

			showForm(req, resp, "insert", form, new LinkedHashMap<String, String>());
			break;
		}
		case "/post/update": {
			Post post = findPost(req.getParameter("id"));

			if (post == null) {
				ControllerUtil.errorMessage(req, "Post não encontrado.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}

			Map<String, String> form = new LinkedHashMap<String, String>();
			form.put("id", String.valueOf(post.getId()));
			form.put("content", post.getContent());
			form.put("user", String.valueOf(post.getUser().getId()));

			showForm(req, resp, "update", form, new LinkedHashMap<String, String>());
			break;
		}
		default:
			list(req, resp);
		}
	}

	// Sobrescreve o método doPost, sendo capaz de responder métodos HTTP POST
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/post/insert":
			save(req, resp, false);
			break;
		case "/post/update":
			save(req, resp, true);
			break;
		case "/post/delete":
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

		PostDAO dao = DAOFactory.createDAO(PostDAO.class);

		try {
			int total = dao.count(search);

			Pagination pagination = Pagination.from(req, LIST_PATH, total).param("q", search);

			List<Post> posts = dao.listPage(search, pagination.getPageSize(), pagination.getOffset());

			req.setAttribute("posts", posts);
			req.setAttribute("pagination", pagination);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			ControllerUtil.errorMessageNow(req, "Não foi possível carregar os posts agora.");
		}

		req.setAttribute("search", search);

		ControllerUtil.transferSessionMessagesToRequest(req);

		ControllerUtil.forward(req, resp, "/posts.jsp");
	}

	// ------------------------------------------------------------------
	// Formulário
	// ------------------------------------------------------------------

	private void showForm(HttpServletRequest req, HttpServletResponse resp, String action,
			Map<String, String> form, Map<String, String> errors) throws ServletException, IOException {

		CommonsController.listUsers(req);

		req.setAttribute("action", action);
		req.setAttribute("form", form);
		req.setAttribute("errors", errors);
		req.setAttribute("back", ControllerUtil.backQuery(req));

		ControllerUtil.forward(req, resp, FORM_PAGE);
	}

	// ------------------------------------------------------------------
	// Inserir / atualizar
	// ------------------------------------------------------------------

	private void save(HttpServletRequest req, HttpServletResponse resp, boolean isUpdate)
			throws ServletException, IOException {

		Map<String, String> form = new LinkedHashMap<String, String>();
		form.put("id", ControllerUtil.trim(req.getParameter("id")));
		form.put("content", ControllerUtil.trim(req.getParameter("content")));
		form.put("user", ControllerUtil.trim(req.getParameter("user")));

		Map<String, String> errors = new LinkedHashMap<String, String>();

		Post post = new Post();

		if (isUpdate) {
			post = findPost(form.get("id"));

			if (post == null) {
				ControllerUtil.errorMessage(req, "Post não encontrado.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}
		}

		// conteúdo
		String content = ControllerUtil.multiline(form.get("content"));
		if (content == null)
			errors.put("content", "Escreva o conteúdo do post.");
		else if (content.length() > 1000)
			errors.put("content", "Use no máximo 1000 caracteres.");
		else
			post.setContent(content);

		// autor (chave estrangeira)
		Integer userId = ControllerUtil.parseInteger(form.get("user"));
		if (userId == null) {
			errors.put("user", "Selecione o autor.");
		} else {
			User author = findUser(userId);
			if (author == null)
				errors.put("user", "Usuário não encontrado. Escolha outro.");
			else
				post.setUser(author);
		}

		if (!errors.isEmpty()) {
			showForm(req, resp, isUpdate ? "update" : "insert", form, errors);
			return;
		}

		if (!isUpdate)
			post.setPostDate(Calendar.getInstance().getTime());

		PostDAO dao = DAOFactory.createDAO(PostDAO.class);

		try {
			boolean ok = isUpdate ? dao.update(post) : dao.save(post);

			if (ok) {
				ControllerUtil.sucessMessage(req, "Post " + (isUpdate ? "atualizado" : "publicado") + " com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Post não pode ser " + (isUpdate ? "atualizado" : "salvo") + ".");
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

	// ------------------------------------------------------------------
	// Excluir
	// ------------------------------------------------------------------

	private void delete(HttpServletRequest req) {
		Post post = findPost(req.getParameter("id"));

		if (post == null) {
			ControllerUtil.errorMessage(req, "Post não encontrado para exclusão.");
			return;
		}

		PostDAO dao = DAOFactory.createDAO(PostDAO.class);

		try {
			if (dao.delete(post)) {
				ControllerUtil.sucessMessage(req, "Post excluído com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Post não pode ser excluído.");
			}
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			ControllerUtil.errorMessage(req, e.getMessage());
		}
	}

	// ------------------------------------------------------------------
	// Buscas por id (devolvem null se não achar ou se o id for inválido)
	// ------------------------------------------------------------------

	private Post findPost(String idParameter) {
		Integer id = ControllerUtil.parseInteger(idParameter);

		if (id == null)
			return null;

		try {
			PostDAO dao = DAOFactory.createDAO(PostDAO.class);
			return dao.findById(id);
		} catch (ModelException e) {
			e.printStackTrace();
			return null;
		}
	}

	private User findUser(int id) {
		try {
			UserDAO dao = DAOFactory.createDAO(UserDAO.class);
			return dao.findById(id);
		} catch (ModelException e) {
			e.printStackTrace();
			return null;
		}
	}
}
