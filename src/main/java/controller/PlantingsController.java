package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.DateUtil;
import model.ModelException;
import model.Planting;
import model.PlantingStatus;
import model.RuralProperty;
import model.User;
import model.dao.DAOFactory;
import model.dao.PlantingDAO;
import model.dao.RuralPropertyDAO;
import model.dao.UserDAO;

/**
 * CRUD de Plantios (atividade 3).
 *
 *   GET  /plantings          lista paginada, com busca e abas por status
 *   GET  /planting/form      formulário de novo plantio
 *   GET  /planting/update    formulário de edição (?id=...)
 *   POST /planting/insert    grava um novo plantio
 *   POST /planting/update    grava a edição
 *   POST /planting/delete    exclui
 *
 * Depois de gravar/excluir, o controller redireciona para a lista (padrão
 * Post/Redirect/Get): se a pessoa apertar F5, o navegador repete só o GET da lista e
 * não reenvia o formulário.
 * Se a validação falhar, o formulário é reexibido com o que foi digitado e as mensagens.
 */
@WebServlet(urlPatterns = { "/plantings", "/planting/form", "/planting/insert", "/planting/update",
		"/planting/delete" })
public class PlantingsController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String LIST_PATH = "/plantings";
	private static final String FORM_PAGE = "/form-planting.jsp";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/planting/form": {
			Map<String, String> form = new LinkedHashMap<String, String>();

			// valores iniciais úteis: plantio planejado, para hoje, sob responsabilidade de quem está logado
			form.put("status", PlantingStatus.PLANNED.name());
			form.put("plantingDate", LocalDate.now().toString());

			User logged = AuthenticationFilter.loggedUser(req);
			if (logged != null)
				form.put("manager", String.valueOf(logged.getId()));

			showForm(req, resp, "insert", form, new LinkedHashMap<String, String>());
			break;
		}
		case "/planting/update": {
			Planting planting = findPlanting(req.getParameter("id"));

			if (planting == null) {
				ControllerUtil.errorMessage(req, "Plantio não encontrado.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}

			showForm(req, resp, "update", toForm(planting), new LinkedHashMap<String, String>());
			break;
		}
		default:
			list(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/planting/insert":
			save(req, resp, false);
			break;
		case "/planting/update":
			save(req, resp, true);
			break;
		case "/planting/delete":
			delete(req);
			ControllerUtil.redirectToList(req, resp, LIST_PATH);
			break;
		default:
			ControllerUtil.redirect(resp, req.getContextPath() + LIST_PATH);
		}
	}

	// ------------------------------------------------------------------
	// Lista com paginação, busca e filtro por status
	// ------------------------------------------------------------------

	private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String search = ControllerUtil.searchTerm(req);
		PlantingStatus status = PlantingStatus.fromCode(req.getParameter("status")); // null = todos

		PlantingDAO dao = DAOFactory.createDAO(PlantingDAO.class);

		try {
			// 1) quantos registros existem para esta busca/filtro
			int total = dao.count(search, status);

			// 2) calcula as páginas (e corrige page/size inválidos vindos da URL)
			Pagination pagination = Pagination.from(req, LIST_PATH, total)
					.param("q", search)
					.param("status", status == null ? null : status.name());

			// 3) busca no banco só os registros da página atual
			List<Planting> plantings = dao.listPage(search, status, pagination.getPageSize(),
					pagination.getOffset());

			// 4) contadores das abas (respeitam a busca, mas não o filtro de status)
			Map<PlantingStatus, Integer> counts = dao.countByStatus(search);
			int all = 0;
			for (int n : counts.values())
				all += n;

			req.setAttribute("plantings", plantings);
			req.setAttribute("pagination", pagination);
			req.setAttribute("statusCounts", counts);
			req.setAttribute("allCount", all);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			ControllerUtil.errorMessageNow(req, "Não foi possível carregar os plantios agora.");
		}

		req.setAttribute("search", search);
		req.setAttribute("status", status == null ? "" : status.name());
		req.setAttribute("statuses", PlantingStatus.values());

		ControllerUtil.transferSessionMessagesToRequest(req);

		ControllerUtil.forward(req, resp, "/plantings.jsp");
	}

	// ------------------------------------------------------------------
	// Formulário
	// ------------------------------------------------------------------

	private void showForm(HttpServletRequest req, HttpServletResponse resp, String action,
			Map<String, String> form, Map<String, String> errors) throws ServletException, IOException {

		// listas dos campos select (chaves estrangeiras): propriedades e responsáveis
		CommonsController.listProperties(req);
		CommonsController.listUsers(req);

		req.setAttribute("action", action);
		req.setAttribute("form", form);
		req.setAttribute("errors", errors);
		req.setAttribute("statuses", PlantingStatus.values());
		req.setAttribute("back", ControllerUtil.backQuery(req));

		ControllerUtil.forward(req, resp, FORM_PAGE);
	}

	private Map<String, String> toForm(Planting p) {
		Map<String, String> form = new LinkedHashMap<String, String>();

		form.put("id", String.valueOf(p.getId()));
		form.put("crop", p.getCrop());
		form.put("variety", p.getVariety() == null ? "" : p.getVariety());
		form.put("plantingDate", ControllerUtil.dateToField(p.getPlantingDate()));
		form.put("expectedHarvest", ControllerUtil.dateToField(p.getExpectedHarvest()));
		form.put("area", ControllerUtil.decimalToField(p.getArea()));
		form.put("expectedYield", ControllerUtil.decimalToField(p.getExpectedYield()));
		form.put("status", p.getStatus().name());
		form.put("notes", p.getNotes() == null ? "" : p.getNotes());
		form.put("property", String.valueOf(p.getProperty().getId()));
		form.put("manager", String.valueOf(p.getManager().getId()));

		return form;
	}

	private Map<String, String> readForm(HttpServletRequest req) {
		Map<String, String> form = new LinkedHashMap<String, String>();

		form.put("id", ControllerUtil.trim(req.getParameter("id")));
		form.put("crop", ControllerUtil.trim(req.getParameter("crop")));
		form.put("variety", ControllerUtil.trim(req.getParameter("variety")));
		form.put("plantingDate", ControllerUtil.trim(req.getParameter("plantingDate")));
		form.put("expectedHarvest", ControllerUtil.trim(req.getParameter("expectedHarvest")));
		form.put("area", ControllerUtil.trim(req.getParameter("area")));
		form.put("expectedYield", ControllerUtil.trim(req.getParameter("expectedYield")));
		form.put("status", ControllerUtil.trim(req.getParameter("status")));
		form.put("notes", ControllerUtil.trim(req.getParameter("notes")));
		form.put("property", ControllerUtil.trim(req.getParameter("property")));
		form.put("manager", ControllerUtil.trim(req.getParameter("manager")));

		return form;
	}

	// ------------------------------------------------------------------
	// Inserir / atualizar
	// ------------------------------------------------------------------

	private void save(HttpServletRequest req, HttpServletResponse resp, boolean isUpdate)
			throws ServletException, IOException {

		Map<String, String> form = readForm(req);
		Map<String, String> errors = new LinkedHashMap<String, String>();

		Planting planting = new Planting();

		if (isUpdate) {
			planting = findPlanting(form.get("id"));

			if (planting == null) {
				ControllerUtil.errorMessage(req, "Plantio não encontrado.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}
		}

		// Preenche o objeto com o que foi digitado e anota os erros de cada campo
		validate(form, planting, errors);

		if (!errors.isEmpty()) {
			showForm(req, resp, isUpdate ? "update" : "insert", form, errors);
			return;
		}

		PlantingDAO dao = DAOFactory.createDAO(PlantingDAO.class);

		try {
			boolean ok = isUpdate ? dao.update(planting) : dao.save(planting);

			if (ok) {
				ControllerUtil.sucessMessage(req, "Plantio de '" + planting.getCrop() + "' em '"
						+ planting.getProperty().getName() + "' " + (isUpdate ? "atualizado" : "cadastrado")
						+ " com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Plantio de '" + planting.getCrop() + "' não pode ser "
						+ (isUpdate ? "atualizado" : "salvo") + ".");
			}
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();

			// volta para o formulário (sem perder o que foi digitado) com o motivo do erro
			ControllerUtil.errorMessageNow(req, e.getMessage());
			showForm(req, resp, isUpdate ? "update" : "insert", form, errors);
			return;
		}

		ControllerUtil.redirectToList(req, resp, LIST_PATH);
	}

	/** Valida o formulário; o que estiver correto vai para o objeto Planting. */
	private void validate(Map<String, String> form, Planting planting, Map<String, String> errors) {

		// cultura (obrigatório, até 80 caracteres)
		String crop = form.get("crop");
		if (crop.isEmpty())
			errors.put("crop", "Informe a cultura.");
		else if (crop.length() > 80)
			errors.put("crop", "Use no máximo 80 caracteres.");
		planting.setCrop(crop);

		// variedade (opcional, até 80)
		String variety = form.get("variety");
		if (variety.length() > 80)
			errors.put("variety", "Use no máximo 80 caracteres.");
		planting.setVariety(variety.isEmpty() ? null : variety);

		// datas
		Date plantingDate = ControllerUtil.formatDate(form.get("plantingDate"));
		Date expectedHarvest = ControllerUtil.formatDate(form.get("expectedHarvest"));

		if (plantingDate == null)
			errors.put("plantingDate", "Informe uma data de plantio válida.");
		if (expectedHarvest == null)
			errors.put("expectedHarvest", "Informe uma data de colheita válida.");
		if (plantingDate != null && expectedHarvest != null && expectedHarvest.before(plantingDate))
			errors.put("expectedHarvest", "A colheita não pode ser antes do plantio.");

		planting.setPlantingDate(plantingDate);
		planting.setExpectedHarvest(expectedHarvest);

		// área plantada (hectares)
		Double area = ControllerUtil.parseDecimal(form.get("area"));
		if (area == null)
			errors.put("area", "Informe a área plantada em hectares.");
		else if (area <= 0)
			errors.put("area", "A área deve ser maior que zero.");
		else if (area > 1000000)
			errors.put("area", "A área informada é grande demais.");
		else
			planting.setArea(area);

		// produtividade esperada (sacas por hectare)
		Double expectedYield = ControllerUtil.parseDecimal(form.get("expectedYield"));
		if (expectedYield == null)
			errors.put("expectedYield", "Informe a produtividade esperada (sacas por hectare).");
		else if (expectedYield < 0)
			errors.put("expectedYield", "A produtividade não pode ser negativa.");
		else if (expectedYield > 100000)
			errors.put("expectedYield", "A produtividade informada é grande demais.");
		else
			planting.setExpectedYield(expectedYield);

		// situação
		PlantingStatus status = PlantingStatus.fromCode(form.get("status"));
		if (status == null)
			errors.put("status", "Escolha a situação do plantio.");
		else
			planting.setStatus(status);

		// observações (opcional)
		String notes = ControllerUtil.multiline(form.get("notes"));
		if (notes != null && notes.length() > 2000)
			errors.put("notes", "Use no máximo 2000 caracteres.");
		planting.setNotes(notes);

		// propriedade (chave estrangeira): precisa existir
		RuralProperty property = null;
		Integer propertyId = ControllerUtil.parseInteger(form.get("property"));
		if (propertyId == null) {
			errors.put("property", "Selecione a propriedade.");
		} else {
			property = findProperty(propertyId);
			if (property == null)
				errors.put("property", "Propriedade não encontrada. Escolha outra.");
			else
				planting.setProperty(property);
		}

		// o plantio não pode ser maior que a propriedade
		if (property != null && area != null && area > 0 && area > property.getArea() && !errors.containsKey("area"))
			errors.put("area", "A área plantada (" + ControllerUtil.hectares(area)
					+ ") é maior que a área da propriedade (" + ControllerUtil.hectares(property.getArea()) + ").");

		// responsável (chave estrangeira): precisa existir
		Integer managerId = ControllerUtil.parseInteger(form.get("manager"));
		if (managerId == null) {
			errors.put("manager", "Selecione o responsável.");
		} else {
			User manager = findUser(managerId);
			if (manager == null)
				errors.put("manager", "Responsável não encontrado. Escolha outro.");
			else
				planting.setManager(manager);
		}
	}

	// ------------------------------------------------------------------
	// Excluir
	// ------------------------------------------------------------------

	private void delete(HttpServletRequest req) {
		Planting planting = findPlanting(req.getParameter("id"));

		if (planting == null) {
			ControllerUtil.errorMessage(req, "Plantio não encontrado para exclusão.");
			return;
		}

		PlantingDAO dao = DAOFactory.createDAO(PlantingDAO.class);

		try {
			if (dao.delete(planting)) {
				ControllerUtil.sucessMessage(req, "Plantio de '" + planting.getCrop() + "' em '"
						+ planting.getProperty().getName() + "' excluído com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Plantio de '" + planting.getCrop() + "' não pode ser excluído.");
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

	private Planting findPlanting(String idParameter) {
		Integer id = ControllerUtil.parseInteger(idParameter);

		if (id == null)
			return null;

		try {
			PlantingDAO dao = DAOFactory.createDAO(PlantingDAO.class);
			return dao.findById(id);
		} catch (ModelException e) {
			e.printStackTrace();
			return null;
		}
	}

	private RuralProperty findProperty(int id) {
		try {
			RuralPropertyDAO dao = DAOFactory.createDAO(RuralPropertyDAO.class);
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
