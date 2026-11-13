package controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Cálculos da paginação de uma lista. Quem usa (os controllers) conta os registros no
 * banco, cria um Pagination e pede ao DAO só a página atual com getPageSize()/getOffset().
 * A tela (fragmento pagination.jspf) usa os getters para desenhar a barra.
 *
 * Exemplo: 29 registros, 10 por página -> 3 páginas; na página 3, getOffset() = 20 e
 * getFirstItem()..getLastItem() = 21..29.
 */
public class Pagination {

	public static final List<Integer> PAGE_SIZES = Collections.unmodifiableList(Arrays.asList(5, 10, 20, 50));
	public static final int DEFAULT_PAGE_SIZE = 10;

	/** Marca de "..." na lista de páginas (getPages). */
	public static final int GAP = -1;

	private final String path;
	private final int page;
	private final int pageSize;
	private final int totalItems;
	private final int totalPages;

	// parâmetros que precisam ser mantidos nos links (q, status...)
	private final Map<String, String> params = new LinkedHashMap<String, String>();

	/**
	 * @param path          endereço da lista, sem o contexto (ex.: "/plantings")
	 * @param requestedPage página pedida na URL (se for inválida, ajusta para o intervalo certo)
	 * @param pageSize      itens por página (se não for uma das opções, usa o padrão)
	 * @param totalItems    quantos registros existem para a busca atual
	 */
	public Pagination(String path, int requestedPage, int pageSize, int totalItems) {
		this.path = path;
		this.pageSize = PAGE_SIZES.contains(pageSize) ? pageSize : DEFAULT_PAGE_SIZE;
		this.totalItems = Math.max(totalItems, 0);
		this.totalPages = Math.max(1, (int) Math.ceil(this.totalItems / (double) this.pageSize));
		this.page = Math.min(Math.max(requestedPage, 1), totalPages);
	}

	/** Lê "page" e "size" da URL e cria o Pagination. */
	public static Pagination from(HttpServletRequest req, String path, int totalItems) {
		int size = ControllerUtil.parseInt(req.getParameter("size"), DEFAULT_PAGE_SIZE);
		int page = ControllerUtil.parseInt(req.getParameter("page"), 1);

		return new Pagination(path, page, size, totalItems);
	}

	/** Guarda um parâmetro (ex.: q) para repetir nos links; valor vazio é ignorado. */
	public Pagination param(String name, String value) {
		if (value != null && !value.isEmpty())
			params.put(name, value);

		return this;
	}

	public String getPath() {
		return path;
	}

	public int getPage() {
		return page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public List<Integer> getPageSizes() {
		return PAGE_SIZES;
	}

	/** Quantos registros pular no banco (LIMIT ? OFFSET ?). */
	public int getOffset() {
		return (page - 1) * pageSize;
	}

	/** Número do primeiro item exibido (0 se a lista estiver vazia). */
	public int getFirstItem() {
		return totalItems == 0 ? 0 : getOffset() + 1;
	}

	public int getLastItem() {
		return Math.min(getOffset() + pageSize, totalItems);
	}

	public boolean isHasPrevious() {
		return page > 1;
	}

	public boolean isHasNext() {
		return page < totalPages;
	}

	/** O seletor "itens por página" só faz sentido se houver mais itens que a menor opção. */
	public boolean isShowPageSize() {
		return totalItems > PAGE_SIZES.get(0);
	}

	/** Só mostra a barra se houver mais de uma página ou o seletor de tamanho. */
	public boolean isVisible() {
		return totalItems > 0 && (totalPages > 1 || isShowPageSize());
	}

	/**
	 * Números das páginas a mostrar, com GAP (-1) onde há "...". Mostra sempre a primeira,
	 * a última e as vizinhas da atual; um "..." nunca esconde uma página só.
	 * Ex.: página 6 de 12 -> 1 ... 5 6 7 ... 12
	 */
	public List<Integer> getPages() {
		TreeSet<Integer> visible = new TreeSet<Integer>();
		visible.add(1);
		visible.add(totalPages);

		for (int p = page - 1; p <= page + 1; p++)
			visible.add(p);

		// perto das pontas, mostra um bloco maior para a barra não "pular" de tamanho
		if (page <= 3)
			for (int p = 1; p <= 4; p++)
				visible.add(p);
		if (page >= totalPages - 2)
			for (int p = totalPages - 3; p <= totalPages; p++)
				visible.add(p);

		List<Integer> pages = new ArrayList<Integer>();
		int previous = 0;

		for (int p : visible) {
			if (p < 1 || p > totalPages)
				continue;

			if (previous != 0 && p - previous == 2)
				pages.add(previous + 1); // um "..." no lugar de uma página só seria bobagem
			else if (previous != 0 && p - previous > 2)
				pages.add(GAP);

			pages.add(p);
			previous = p;
		}

		return pages;
	}

	/**
	 * Query string do estado atual da lista (q, status, size e page), já codificada.
	 * É o que os formulários guardam no campo "back" para voltar à mesma página.
	 */
	public String getQuery() {
		return queryFor(page);
	}

	/** Endereço (sem o contexto) da lista na página pedida, mantendo busca e filtros. */
	public String urlFor(int targetPage) {
		String query = queryFor(targetPage);

		return path + (query.isEmpty() ? "" : "?" + query);
	}

	private String queryFor(int targetPage) {
		StringBuilder query = new StringBuilder();

		for (Map.Entry<String, String> param : params.entrySet())
			append(query, param.getKey(), param.getValue());

		if (pageSize != DEFAULT_PAGE_SIZE)
			append(query, "size", String.valueOf(pageSize));
		if (targetPage != 1)
			append(query, "page", String.valueOf(targetPage));

		return query.toString();
	}

	private void append(StringBuilder query, String name, String value) {
		if (query.length() > 0)
			query.append('&');

		try {
			query.append(name).append('=').append(URLEncoder.encode(value, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // UTF-8 sempre existe
		}
	}
}
