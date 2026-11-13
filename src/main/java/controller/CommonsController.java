package controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import model.ModelException;
import model.RuralProperty;
import model.User;
import model.dao.DAOFactory;
import model.dao.RuralPropertyDAO;
import model.dao.UserDAO;

/**
 * Carrega listas completas usadas nos campos select (chave estrangeira) dos formulários.
 */
public class CommonsController {

	public static void listUsers(HttpServletRequest req) {
		UserDAO dao = DAOFactory.createDAO(UserDAO.class);

		List<User> listUsers = null;
		try {
			listUsers = dao.listAll();
		} catch (ModelException e) {
			// Log no servidor
			e.printStackTrace();
		}

		if (listUsers != null)
			req.setAttribute("users", listUsers);
	}

	// Atividade 3: o formulário de plantio escolhe a propriedade em um select
	public static void listProperties(HttpServletRequest req) {
		RuralPropertyDAO dao = DAOFactory.createDAO(RuralPropertyDAO.class);

		List<RuralProperty> properties = null;
		try {
			properties = dao.listAll();
		} catch (ModelException e) {
			// Log no servidor
			e.printStackTrace();
		}

		if (properties != null)
			req.setAttribute("properties", properties);
	}
}
