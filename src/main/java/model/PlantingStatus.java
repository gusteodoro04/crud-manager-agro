package model;

/**
 * Situação de um plantio. O nome da constante é o que vai para a coluna
 * status do banco (ENUM). O cssClass escolhe a cor do selo (chip) nas telas.
 */
public enum PlantingStatus {

	PLANNED("Planejado", "planned"),
	GROWING("Em desenvolvimento", "growing"),
	HARVESTED("Colhido", "harvested"),
	LOST("Perdido", "lost");

	private final String label;
	private final String cssClass;

	private PlantingStatus(String label, String cssClass) {
		this.label = label;
		this.cssClass = cssClass;
	}

	public String getLabel() {
		return label;
	}

	public String getCssClass() {
		return cssClass;
	}

	/** Converte o texto vindo do formulário/banco; devolve null se for inválido. */
	public static PlantingStatus fromCode(String code) {
		if (code == null)
			return null;

		for (PlantingStatus status : values()) {
			if (status.name().equalsIgnoreCase(code.trim()))
				return status;
		}
		return null;
	}
}
