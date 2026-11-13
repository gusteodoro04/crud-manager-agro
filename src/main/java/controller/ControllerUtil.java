package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.regex.Pattern;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.DateUtil;

public class ControllerUtil {

	private static String MESSAGE_KEY = "message";
	private static int ERROR = 0;
	private static int SUCESS = 1;

	// "back" guarda o estado da lista (busca, página, filtro) para voltar a ele depois de
	// salvar/excluir. Só aceitamos letras, números e símbolos comuns de URL: assim o valor
	// nunca vira outro endereço (open redirect) nem quebra um cabeçalho HTTP.
	private static final Pattern SAFE_QUERY = Pattern.compile("[A-Za-z0-9=&%._~+\\-]{0,400}");

	static void forward(HttpServletRequest req, HttpServletResponse resp, String target)
			throws ServletException, IOException {
		RequestDispatcher dispatcher = req.getRequestDispatcher(target);

		dispatcher.forward(req, resp);
	}

	static void redirect(HttpServletResponse resp, String target) throws IOException {
		resp.sendRedirect(target);
	}

	static void sucessMessage(HttpServletRequest req, String message) {
		req.getSession().setAttribute(MESSAGE_KEY, message);
		req.getSession().setAttribute("alertType", SUCESS);
	}

	static void errorMessage(HttpServletRequest req, String message) {
		req.getSession().setAttribute(MESSAGE_KEY, message);
		req.getSession().setAttribute("alertType", ERROR);
	}

	/** Mensagem de erro para a tela que está sendo desenhada agora (sem redirecionar). */
	static void errorMessageNow(HttpServletRequest req, String message) {
		req.setAttribute(MESSAGE_KEY, message);
		req.setAttribute("alertType", ERROR);
	}

	static void transferSessionMessagesToRequest(HttpServletRequest req) {
		if (req.getSession().getAttribute(MESSAGE_KEY) != null) {
			String message = (String) req.getSession().getAttribute(MESSAGE_KEY);
			req.setAttribute(MESSAGE_KEY, message);
			req.getSession().removeAttribute(MESSAGE_KEY);
		}

		if (req.getSession().getAttribute("alertType") != null) {
			Integer message = (Integer) req.getSession().getAttribute("alertType");
			req.setAttribute("alertType", message);
			req.getSession().removeAttribute("alertType");
		}
	}

	// ------------------------------------------------------------------
	// Voltar para a lista no mesmo ponto (busca/página) depois de uma ação
	// ------------------------------------------------------------------

	/** Estado da lista que veio escondido no formulário (campo "back"), já validado. */
	static String backQuery(HttpServletRequest req) {
		String back = req.getParameter("back");

		if (back != null && SAFE_QUERY.matcher(back).matches())
			return back;

		return "";
	}

	/** Redireciona para a lista (PRG) mantendo busca, página e filtro de antes. */
	static void redirectToList(HttpServletRequest req, HttpServletResponse resp, String listPath)
			throws IOException {
		String back = backQuery(req);

		resp.sendRedirect(req.getContextPath() + listPath + (back.isEmpty() ? "" : "?" + back));
	}

	// ------------------------------------------------------------------
	// Leitura de parâmetros do formulário
	// ------------------------------------------------------------------

	/** Texto sem espaços nas pontas; nunca devolve null. */
	static String trim(String value) {
		return value == null ? "" : value.trim();
	}

	/** Texto sem espaços nas pontas; campo vazio vira null (grava NULL no banco). */
	static String trimToNull(String value) {
		String trimmed = trim(value);
		return trimmed.isEmpty() ? null : trimmed;
	}

	/** Texto de várias linhas (textarea): quebras de linha padronizadas; vazio vira null. */
	static String multiline(String value) {
		return trimToNull(value == null ? null : value.replace("\r\n", "\n").replace('\r', '\n'));
	}

	/** Corta o texto no tamanho máximo (usado na busca, para não aceitar textos gigantes). */
	static String limit(String value, int max) {
		return value.length() > max ? value.substring(0, max) : value;
	}

	/** Termo de busca digitado na caixa de pesquisa (parâmetro "q"). */
	static String searchTerm(HttpServletRequest req) {
		return limit(trim(req.getParameter("q")), 100);
	}

	/** 30.25 -> "30,25 ha" (para mensagens ao usuário). */
	static String hectares(double value) {
		java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance(new java.util.Locale("pt", "BR"));
		format.setMaximumFractionDigits(2);
		return format.format(value) + " ha";
	}

	/** Inteiro do parâmetro, ou o valor padrão se vier vazio/inválido. */
	static int parseInt(String value, int fallback) {
		Integer parsed = parseInteger(value);
		return parsed == null ? fallback : parsed;
	}

	/** Inteiro do parâmetro, ou null se vier vazio/inválido. */
	static Integer parseInteger(String value) {
		try {
			return Integer.valueOf(trim(value));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Número decimal ("12.5" ou "12,5"), ou null se vier vazio/inválido. */
	static Double parseDecimal(String value) {
		String text = trim(value).replace(" ", "");

		if (text.isEmpty())
			return null;

		// "1.234,5" -> "1234.5"; "12,5" -> "12.5"
		if (text.contains(","))
			text = text.replace(".", "").replace(",", ".");

		try {
			double number = Double.parseDouble(text);

			if (Double.isNaN(number) || Double.isInfinite(number))
				return null;

			return number;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Data no formato do input type="date" (yyyy-MM-dd), ou null se vier vazia/inválida.
	 * Usa LocalDate: não aceita "2026-13-45" (o SimpleDateFormat do projeto base aceitava e
	 * "corrigia" para outra data) e recusa anos absurdos que o navegador deixa digitar.
	 */
	public static Date formatDate(String stringDate) {

		String text = trim(stringDate);

		if (text.isEmpty())
			return null;

		try {
			LocalDate date = LocalDate.parse(text);

			if (date.getYear() < 1900 || date.getYear() > 2200)
				return null;

			return DateUtil.toDate(date);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	// ------------------------------------------------------------------
	// Valores para preencher campos de formulário (sempre texto)
	// ------------------------------------------------------------------

	/** 12.5 -> "12.5"; 30.0 -> "30" (formato que o input type="number" aceita). */
	static String decimalToField(double value) {
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
	}

	/** Date -> "yyyy-MM-dd" (formato do input type="date"); null vira "". */
	static String dateToField(Date date) {
		return date == null ? "" : DateUtil.toLocalDate(date).toString();
	}
}
