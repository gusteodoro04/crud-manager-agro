package model;

import java.io.Serializable;

/**
 * Usuário do sistema. Agora também é quem faz LOGIN.
 *
 * Implementa Serializable porque o usuário logado fica guardado na sessão
 * HTTP (e o Tomcat pode gravar a sessão em disco ao reiniciar).
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String name;
	private String gender;
	private String email;

	// Hash PBKDF2 da senha (veja PasswordUtil). Só é preenchido quando o DAO
	// precisa dele (login, cadastro, troca de senha). Nunca guardamos a senha pura.
	private String passwordHash;

	public User() {
		this(0);
	}

	public User(int id) {
		this.id = id;
		this.name = "";
		this.gender = "";
		this.email = "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getId() {
		return id;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	/** Primeiro nome, usado na saudação do painel ("Bom dia, Emerson"). */
	public String getFirstName() {
		String trimmed = name == null ? "" : name.trim();
		if (trimmed.isEmpty())
			return "";
		return trimmed.split("\\s+")[0];
	}

	/** Iniciais (até 2 letras) para o avatar do cabeçalho. */
	public String getInitials() {
		String trimmed = name == null ? "" : name.trim();
		if (trimmed.isEmpty())
			return "?";

		String[] parts = trimmed.split("\\s+");
		String initials = parts[0].substring(0, 1);
		if (parts.length > 1)
			initials += parts[parts.length - 1].substring(0, 1);

		return initials.toUpperCase();
	}
}
