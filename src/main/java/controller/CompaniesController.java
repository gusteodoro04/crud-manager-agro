package controller;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Company;
import model.ModelException;
import model.User;
import model.dao.CompanyDAO;
import model.dao.DAOFactory;
import model.dao.UserDAO;

/**
 * CRUD de Empresas. Na atividade 3 ganhou paginação, busca e validação dos campos
 * (incluindo "a saída não pode ser antes do início").
 */
@WebServlet(urlPatterns = {"/companies", "/company/form",
		"/company/insert", "/company/delete", "/company/update"})
public class CompaniesController extends HttpServlet{

	private static final long serialVersionUID = 1L;

	private static final String LIST_PATH = "/companies";
	private static final String FORM_PAGE = "/form-company.jsp";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/company/form": {
			Map<String, String> form = new LinkedHashMap<String, String>();

			User logged = AuthenticationFilter.loggedUser(req);
			if (logged != null)
				form.put("user", String.valueOf(logged.getId()));

			showForm(req, resp, "insert", form, new LinkedHashMap<String, String>());
			break;
		}
		case "/company/update": {
			Company company = findCompany(req.getParameter("id"));

			if (company == null) {
				ControllerUtil.errorMessage(req, "Empresa não encontrada.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}

			Map<String, String> form = new LinkedHashMap<String, String>();
			form.put("id", String.valueOf(company.getId()));
			form.put("name", company.getName());
			form.put("role", company.getRole());
			form.put("start", ControllerUtil.dateToField(company.getStart()));
			form.put("end", ControllerUtil.dateToField(company.getEnd()));
			form.put("user", String.valueOf(company.getUser().getId()));

			showForm(req, resp, "update", form, new LinkedHashMap<String, String>());
			break;
		}
		default:
			list(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/company/insert":
			save(req, resp, false);
			break;
		case "/company/update":
			save(req, resp, true);
			break;
		case "/company/delete":
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

		CompanyDAO dao = DAOFactory.createDAO(CompanyDAO.class);

		try {
			int total = dao.count(search);

			Pagination pagination = Pagination.from(req, LIST_PATH, total).param("q", search);

			List<Company> companies = dao.listPage(search, pagination.getPageSize(), pagination.getOffset());

			req.setAttribute("companies", companies);
			req.setAttribute("pagination", pagination);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			ControllerUtil.errorMessageNow(req, "Não foi possível carregar as empresas agora.");
		}

		req.setAttribute("search", search);

		ControllerUtil.transferSessionMessagesToRequest(req);

		ControllerUtil.forward(req, resp, "/companies.jsp");
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
		form.put("name", ControllerUtil.trim(req.getParameter("name")));
		form.put("role", ControllerUtil.trim(req.getParameter("role")));
		form.put("start", ControllerUtil.trim(req.getParameter("start")));
		form.put("end", ControllerUtil.trim(req.getParameter("end")));
		form.put("user", ControllerUtil.trim(req.getParameter("user")));

		Map<String, String> errors = new LinkedHashMap<String, String>();

		Company company = new Company();

		if (isUpdate) {
			company = findCompany(form.get("id"));

			if (company == null) {
				ControllerUtil.errorMessage(req, "Empresa não encontrada.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}
		}

		validate(form, company, errors);

		if (!errors.isEmpty()) {
			showForm(req, resp, isUpdate ? "update" : "insert", form, errors);
			return;
		}

		CompanyDAO dao = DAOFactory.createDAO(CompanyDAO.class);

		try {
			boolean ok = isUpdate ? dao.update(company) : dao.save(company);

			if (ok) {
				ControllerUtil.sucessMessage(req, "Empresa '" + company.getName() + "' "
						+ (isUpdate ? "atualizada" : "salva") + " com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Empresa '" + company.getName() + "' não pode ser "
						+ (isUpdate ? "atualizada" : "salva") + ".");
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

	private void validate(Map<String, String> form, Company company, Map<String, String> errors) {

		String name = form.get("name");
		if (name.isEmpty())
			errors.put("name", "Informe o nome da empresa.");
		else if (name.length() > 128)
			errors.put("name", "Use no máximo 128 caracteres.");
		company.setName(name);

		String role = form.get("role");
		if (role.isEmpty())
			errors.put("role", "Informe o cargo.");
		else if (role.length() > 128)
			errors.put("role", "Use no máximo 128 caracteres.");
		company.setRole(role);

		Date start = ControllerUtil.formatDate(form.get("start"));
		if (start == null)
			errors.put("start", "Informe uma data de início válida.");
		company.setStart(start);

		// saída é opcional (quem ainda trabalha lá não tem data de saída)
		Date end = null;
		if (!form.get("end").isEmpty()) {
			end = ControllerUtil.formatDate(form.get("end"));

			if (end == null)
				errors.put("end", "Informe uma data de saída válida ou deixe em branco.");
			else if (start != null && end.before(start))
				errors.put("end", "A saída não pode ser antes do início.");
		}
		company.setEnd(end);

		Integer userId = ControllerUtil.parseInteger(form.get("user"));
		if (userId == null) {
			errors.put("user", "Selecione o usuário.");
		} else {
			User user = findUser(userId);
			if (user == null)
				errors.put("user", "Usuário não encontrado. Escolha outro.");
			else
				company.setUser(user);
		}
	}

	// ------------------------------------------------------------------
	// Excluir
	// ------------------------------------------------------------------

	private void delete(HttpServletRequest req) {
		Company company = findCompany(req.getParameter("id"));

		if (company == null) {
			ControllerUtil.errorMessage(req, "Empresa não encontrada para exclusão.");
			return;
		}

		CompanyDAO dao = DAOFactory.createDAO(CompanyDAO.class);

		try {
			if (dao.delete(company)) {
				ControllerUtil.sucessMessage(req, "Empresa '" + company.getName() + "' excluída com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Empresa '" + company.getName() + "' não pode ser excluída.");
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

	private Company findCompany(String idParameter) {
		Integer id = ControllerUtil.parseInteger(idParameter);

		if (id == null)
			return null;

		try {
			CompanyDAO dao = DAOFactory.createDAO(CompanyDAO.class);
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
