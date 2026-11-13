package model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import model.ModelException;

public class MySQLConnectionFactory {

	// Nome do Driver (classe) e URL do banco
	// (com.mysql.cj.jdbc.Driver é o nome atual no Connector/J 8; o antigo
	// com.mysql.jdbc.Driver ainda funciona, mas gera um aviso no log)
	private static final String JDBC_DRIVER_NAME =
			"com.mysql.cj.jdbc.Driver";

//	private static final String DATABASE_URL =
//			"jdbc:mysql://127.0.0.1/facebook";
	// useUnicode/characterEncoding: acentos corretos.
	// serverTimezone: evita o erro "server time zone value ... is unrecognized"
	// em Windows com idioma português.
	private static final String DATABASE_URL =
			"jdbc:mysql://localhost:3306/crud_manager?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

	// Credenciais (ajuste se a senha do seu MySQL for outra)
	private static final String USER = "root";
	private static final String PASSWORD = "root1234";

	public static Connection getConnection() throws ModelException {
		// API JDBC < 3 e Java < 5
		try {
			Class.forName(JDBC_DRIVER_NAME);

			// Cria uma conexão com o Banco de Dados
			return DriverManager.getConnection(
					DATABASE_URL, USER, PASSWORD);
		} catch (ClassNotFoundException e) {
			throw new ModelException("Driver não encontrado", e);
		} catch (SQLException e) {
			throw new ModelException("Falha na conexão com o BD", e);
		}
	}
}
