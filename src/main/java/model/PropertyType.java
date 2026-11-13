package model;

/**
 * Tipos de propriedade rural. O nome da constante (FAZENDA, SITIO...) é o que
 * fica gravado na coluna property_type do banco (ENUM). O rótulo e a dica são
 * só para exibir nas telas.
 */
public enum PropertyType {

	FAZENDA("Fazenda", "Grandes áreas de produção"),
	SITIO("Sítio", "Pequena e média produção"),
	CHACARA("Chácara", "Lazer e produção familiar"),
	GRANJA("Granja", "Aves, suínos e hortigranjeiros");

	private final String label;
	private final String hint;

	private PropertyType(String label, String hint) {
		this.label = label;
		this.hint = hint;
	}

	public String getLabel() {
		return label;
	}

	public String getHint() {
		return hint;
	}

	/** Converte o texto vindo do formulário/banco; devolve null se for inválido. */
	public static PropertyType fromCode(String code) {
		if (code == null)
			return null;

		for (PropertyType type : values()) {
			if (type.name().equalsIgnoreCase(code.trim()))
				return type;
		}
		return null;
	}
}
