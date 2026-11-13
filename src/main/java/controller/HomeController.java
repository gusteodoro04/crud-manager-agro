package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CropArea;
import model.DashboardSummary;
import model.DateUtil;
import model.ModelException;
import model.Planting;
import model.dao.DAOFactory;
import model.dao.DashboardDAO;

/**
 * Painel inicial (a raiz do sistema). Reúne os números da safra: o que está em campo, as
 * próximas colheitas, o calendário da safra e a área por cultura.
 * O mapeamento "" é a raiz do projeto (http://localhost:8080/crud-manager/).
 */
@WebServlet(urlPatterns = { "", "/home" })
public class HomeController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/** Máximo de plantios no calendário (mais que isso deixaria a página enorme). */
	private static final int CALENDAR_MAX = 60;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		LocalDate today = LocalDate.now();
		Date todayDate = DateUtil.toDate(today);

		DashboardDAO dao = DAOFactory.createDAO(DashboardDAO.class);

		try {
			// 1) números gerais
			DashboardSummary summary = dao.summary(todayDate);

			// 2) próximas colheitas (plantios em desenvolvimento, da data mais próxima para a mais distante)
			List<Planting> nextHarvests = dao.nextHarvests(5);

			// 3) calendário: plantios que encostam na safra atual (1º de julho a 30 de junho)
			Date from = DateUtil.toDate(SeasonCalendar.startOf(today));
			Date to = DateUtil.toDate(SeasonCalendar.endOf(today));
			// pede um a mais que o máximo só para saber se houve corte
			List<Planting> season = dao.timeline(from, to, CALENDAR_MAX + 1);
			boolean truncated = season.size() > CALENDAR_MAX;
			if (truncated)
				season = season.subList(0, CALENDAR_MAX);
			SeasonCalendar calendar = new SeasonCalendar(today, season, truncated);

			// 4) área por cultura; a maior vira a barra de 100%
			List<CropArea> crops = dao.areaByCrop(6);
			if (!crops.isEmpty()) {
				double max = crops.get(0).getArea();
				for (CropArea crop : crops)
					crop.setPercent(max <= 0 ? 0 : crop.getArea() * 100.0 / max);
			}

			req.setAttribute("summary", summary);
			req.setAttribute("nextHarvests", nextHarvests);
			req.setAttribute("calendar", calendar);
			req.setAttribute("crops", crops);
		} catch (ModelException e) {
			// log no servidor
			e.printStackTrace();
			ControllerUtil.errorMessageNow(req, "Não foi possível carregar o painel agora.");
		}

		req.setAttribute("greeting", greeting());

		ControllerUtil.transferSessionMessagesToRequest(req);

		ControllerUtil.forward(req, resp, "/home.jsp");
	}

	private String greeting() {
		int hour = LocalTime.now().getHour();

		if (hour < 12)
			return "Bom dia";
		if (hour < 18)
			return "Boa tarde";
		return "Boa noite";
	}
}
