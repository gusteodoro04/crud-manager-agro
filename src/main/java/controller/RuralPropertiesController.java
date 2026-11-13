package controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import model.BrazilianStates;
import model.DateUtil;
import model.ModelException;
import model.PropertyType;
import model.RuralProperty;
import model.User;
import model.dao.DAOFactory;
import model.dao.RuralPropertyDAO;
import model.dao.UserDAO;

/**
 * CRUD de Propriedades Rurais (atividade 3). Mesma estrutura do PlantingsController:
 *
 *   GET  /properties         lista paginada, com busca
 *   GET  /property/form      formulário de nova propriedade
 *   GET  /property/update    formulário de edição (?id=...)
 *   POST /property/insert    grava
 *   POST /property/update    grava a edição
 *   POST /property/delete    exclui (só se não houver plantios nela)
 */
@WebServlet(urlPatterns = { "/properties", "/property/form", "/property/insert", "/property/update",
		"/property/delete" })
public class RuralPropertiesController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String LIST_PATH = "/properties";
	private static final String FORM_PAGE = "/form-rural-property.jsp";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/property/form": {
			Map<String, String> form = new LinkedHashMap<String, String>();

			// valores iniciais: fazenda, em Minas Gerais, sob responsabilidade de quem está logado
			form.put("type", PropertyType.FAZENDA.name());
			form.put("state", "MG");

			User logged = AuthenticationFilter.loggedUser(req);
			if (logged != null)
				form.put("owner", String.valueOf(logged.getId()));

			showForm(req, resp, "insert", form, new LinkedHashMap<String, String>());
			break;
		}
		case "/property/update": {
			RuralProperty property = findProperty(req.getParameter("id"));

			if (property == null) {
				ControllerUtil.errorMessage(req, "Propriedade não encontrada.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}

			showForm(req, resp, "update", toForm(property), new LinkedHashMap<String, String>());
			break;
		}
		default:
			list(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		switch (req.getServletPath()) {
		case "/property/insert":
			save(req, resp, false);
			break;
		case "/property/update":
			save(req, resp, true);
			break;
		case "/property/delete":
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

		RuralPropertyDAO dao = DAOFactory.createDAO(RuralPropertyDAO.class);

		try {
			int total = dao.count(search);

			Pagination pagination = Pagination.from(req, LIST_PATH, total).param("q", search);

			List<RuralProperty> properties = dao.listPage(search, pagination.getPageSize(),
					pagination.getOffset());

			req.setAttribute("properties", properties);
			req.setAttribute("pagination", pagination);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			ControllerUtil.errorMessageNow(req, "Não foi possível carregar as propriedades agora.");
		}

		req.setAttribute("search", search);

		ControllerUtil.transferSessionMessagesToRequest(req);

		ControllerUtil.forward(req, resp, "/rural-properties.jsp");
	}

	// ------------------------------------------------------------------
	// Formulário
	// ------------------------------------------------------------------

	private void showForm(HttpServletRequest req, HttpServletResponse resp, String action,
			Map<String, String> form, Map<String, String> errors) throws ServletException, IOException {

		// lista de usuários do campo select (chave estrangeira: dono da propriedade)
		CommonsController.listUsers(req);

		req.setAttribute("action", action);
		req.setAttribute("form", form);
		req.setAttribute("errors", errors);
		req.setAttribute("types", PropertyType.values());
		req.setAttribute("states", BrazilianStates.ALL);
		req.setAttribute("back", ControllerUtil.backQuery(req));

		ControllerUtil.forward(req, resp, FORM_PAGE);
	}

	private Map<String, String> toForm(RuralProperty p) {
		Map<String, String> form = new LinkedHashMap<String, String>();

		form.put("id", String.valueOf(p.getId()));
		form.put("name", p.getName());
		form.put("type", p.getType().name());
		form.put("city", p.getCity());
		form.put("state", p.getState());
		form.put("area", ControllerUtil.decimalToField(p.getArea()));
		form.put("acquiredOn", ControllerUtil.dateToField(p.getAcquiredOn()));
		form.put("irrigated", p.isIrrigated() ? "on" : "");
		form.put("mapUrl", p.getMapUrl() == null ? "" : p.getMapUrl());
		form.put("notes", p.getNotes() == null ? "" : p.getNotes());
		form.put("owner", String.valueOf(p.getOwner().getId()));

		return form;
	}

	private Map<String, String> readForm(HttpServletRequest req) {
		Map<String, String> form = new LinkedHashMap<String, String>();

		form.put("id", ControllerUtil.trim(req.getParameter("id")));
		form.put("name", ControllerUtil.trim(req.getParameter("name")));
		form.put("type", ControllerUtil.trim(req.getParameter("type")));
		form.put("city", ControllerUtil.trim(req.getParameter("city")));
		form.put("state", ControllerUtil.trim(req.getParameter("state")));
		form.put("area", ControllerUtil.trim(req.getParameter("area")));
		form.put("acquiredOn", ControllerUtil.trim(req.getParameter("acquiredOn")));
		// checkbox: o navegador só envia o campo quando está marcado
		form.put("irrigated", req.getParameter("irrigated") != null ? "on" : "");
		form.put("mapUrl", ControllerUtil.trim(req.getParameter("mapUrl")));
		form.put("notes", ControllerUtil.trim(req.getParameter("notes")));
		form.put("owner", ControllerUtil.trim(req.getParameter("owner")));

		return form;
	}

	// ------------------------------------------------------------------
	// Inserir / atualizar
	// ------------------------------------------------------------------

	private void save(HttpServletRequest req, HttpServletResponse resp, boolean isUpdate)
			throws ServletException, IOException {

		Map<String, String> form = readForm(req);
		Map<String, String> errors = new LinkedHashMap<String, String>();

		RuralProperty property = new RuralProperty();

		if (isUpdate) {
			property = findProperty(form.get("id"));

			if (property == null) {
				ControllerUtil.errorMessage(req, "Propriedade não encontrada.");
				ControllerUtil.redirectToList(req, resp, LIST_PATH);
				return;
			}
		}

		validate(form, property, errors);

		if (!errors.isEmpty()) {
			showForm(req, resp, isUpdate ? "update" : "insert", form, errors);
			return;
		}

		RuralPropertyDAO dao = DAOFactory.createDAO(RuralPropertyDAO.class);

		try {
			boolean ok = isUpdate ? dao.update(property) : dao.save(property);

			if (ok) {
				ControllerUtil.sucessMessage(req, "Propriedade '" + property.getName() + "' "
						+ (isUpdate ? "atualizada" : "cadastrada") + " com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Propriedade '" + property.getName() + "' não pode ser "
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

	/** Valida o formulário; o que estiver correto vai para o objeto RuralProperty. */
	private void validate(Map<String, String> form, RuralProperty property, Map<String, String> errors) {

		// nome (obrigatório, até 120)
		String name = form.get("name");
		if (name.isEmpty())
			errors.put("name", "Informe o nome da propriedade.");
		else if (name.length() > 120)
			errors.put("name", "Use no máximo 120 caracteres.");
		property.setName(name);

		// tipo (radio)
		PropertyType type = PropertyType.fromCode(form.get("type"));
		if (type == null)
			errors.put("type", "Escolha o tipo da propriedade.");
		else
			property.setType(type);

		// cidade
		String city = form.get("city");
		if (city.isEmpty())
			errors.put("city", "Informe a cidade.");
		else if (city.length() > 100)
			errors.put("city", "Use no máximo 100 caracteres.");
		property.setCity(city);

		// estado (select)
		String state = form.get("state");
		if (!BrazilianStates.isValid(state))
			errors.put("state", "Escolha o estado.");
		else
			property.setState(state);

		// área em hectares
		Double area = ControllerUtil.parseDecimal(form.get("area"));
		if (area == null)
			errors.put("area", "Informe a área em hectares.");
		else if (area <= 0)
			errors.put("area", "A área deve ser maior que zero.");
		else if (area > 1000000)
			errors.put("area", "A área informada é grande demais.");
		else
			property.setArea(area);

		// data de aquisição (opcional, não pode ser no futuro)
		String acquiredText = form.get("acquiredOn");
		if (acquiredText.isEmpty()) {
			property.setAcquiredOn(null);
		} else {
			Date acquiredOn = ControllerUtil.formatDate(acquiredText);

			if (acquiredOn == null)
				errors.put("acquiredOn", "Informe uma data válida.");
			else if (DateUtil.toLocalDate(acquiredOn).isAfter(LocalDate.now()))
				errors.put("acquiredOn", "A data de aquisição não pode ser no futuro.");
			else
				property.setAcquiredOn(acquiredOn);
		}

		// irrigação (checkbox)
		property.setIrrigated("on".equals(form.get("irrigated")));

		// link do mapa (opcional; só aceitamos http/https, nunca "javascript:" etc.)
		String mapUrl = form.get("mapUrl");
		if (mapUrl.isEmpty()) {
			property.setMapUrl(null);
		} else if (mapUrl.length() > 500) {
			errors.put("mapUrl", "Use no máximo 500 caracteres.");
		} else if (!isWebAddress(mapUrl)) {
			errors.put("mapUrl", "Informe um endereço que comece com http:// ou https://.");
		} else {
			property.setMapUrl(mapUrl);
		}

		// observações (opcional)
		String notes = ControllerUtil.multiline(form.get("notes"));
		if (notes != null && notes.length() > 2000)
			errors.put("notes", "Use no máximo 2000 caracteres.");
		property.setNotes(notes);

		// dono (chave estrangeira): precisa existir
		Integer ownerId = ControllerUtil.parseInteger(form.get("owner"));
		if (ownerId == null) {
			errors.put("owner", "Selecione o dono da propriedade.");
		} else {
			User owner = findUser(ownerId);
			if (owner == null)
				errors.put("owner", "Usuário não encontrado. Escolha outro.");
			else
				property.setOwner(owner);
		}
	}

	private boolean isWebAddress(String text) {
		try {
			URI uri = new URI(text);
			String scheme = uri.getScheme();

			return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
					&& uri.getHost() != null;
		} catch (URISyntaxException e) {
			return false;
		}
	}

	// ------------------------------------------------------------------
	// Excluir
	// ------------------------------------------------------------------

	private void delete(HttpServletRequest req) {
		RuralProperty property = findProperty(req.getParameter("id"));

		if (property == null) {
			ControllerUtil.errorMessage(req, "Propriedade não encontrada para exclusão.");
			return;
		}

		// o banco recusaria (chave estrangeira); avisamos antes, com uma mensagem melhor
		if (property.getPlantingCount() > 0) {
			ControllerUtil.errorMessage(req, "A propriedade '" + property.getName() + "' tem "
					+ property.getPlantingCount() + (property.getPlantingCount() == 1 ? " plantio" : " plantios")
					+ " e não pode ser excluída. Exclua ou mude os plantios antes.");
			return;
		}

		RuralPropertyDAO dao = DAOFactory.createDAO(RuralPropertyDAO.class);

		try {
			if (dao.delete(property)) {
				ControllerUtil.sucessMessage(req, "Propriedade '" + property.getName() + "' excluída com sucesso.");
			} else {
				ControllerUtil.errorMessage(req, "Propriedade '" + property.getName() + "' não pode ser excluída.");
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

	private RuralProperty findProperty(String idParameter) {
		Integer id = ControllerUtil.parseInteger(idParameter);

		if (id == null)
			return null;

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
