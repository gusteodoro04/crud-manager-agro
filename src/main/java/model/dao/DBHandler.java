package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import model.ModelException;

/**
 * Cuida de uma conexão com o banco, do PreparedStatement e do ResultSet.
 *
 * Mudanças da atividade 3 (em relação ao projeto base):
 *  - close() agora é público e pode ser chamado mais de uma vez;
 *  - next() fecha tudo sozinho quando acabam as linhas. Antes, as consultas
 *    nunca fechavam a conexão ("vazamento"), e com paginação isso apareceria
 *    rápido como "Too many connections";
 *  - novos setDouble/setBoolean/getDouble/getBoolean (área, irrigação...);
 *  - mensagens de erro amigáveis para chave estrangeira, duplicidade e CHECK;
 *  - like(): monta o padrão do LIKE já protegido contra % e _ digitados.
 */
public class DBHandler {
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private boolean closed = false;

	public DBHandler() throws ModelException {
		connection = MySQLConnectionFactory.getConnection();
	}

	public void prepareStatement(String sqlInsert) throws ModelException {
		try {
			preparedStatement = connection.prepareStatement(sqlInsert);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao preparar a SQL", e);
		}
	}

	public void setString(int i, String value) throws ModelException {
		validatePreparedStatement();

		try {
			preparedStatement.setString(i, value);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao atribuir string", e);
		}
	}

	public void setNullDate(int i) throws ModelException {
		validatePreparedStatement();

		try {
			preparedStatement.setNull(i, java.sql.Types.DATE);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao atribuir null", e);
		}
	}

	public void setDate(int i, Date value) throws ModelException {
		validatePreparedStatement();

		try {
			java.sql.Date date = new java.sql.Date(value.getTime());
			preparedStatement.setDate(i, date);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao atribuir data", e);
		}
	}

	public void setInt(int i, int value) throws ModelException {
		validatePreparedStatement();

		try {
			preparedStatement.setInt(i, value);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao atribuir inteiro", e);
		}
	}

	public void setDouble(int i, double value) throws ModelException {
		validatePreparedStatement();

		try {
			preparedStatement.setDouble(i, value);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao atribuir número decimal", e);
		}
	}

	public void setBoolean(int i, boolean value) throws ModelException {
		validatePreparedStatement();

		try {
			preparedStatement.setBoolean(i, value);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao atribuir booleano", e);
		}
	}

	public int executeUpdate() throws ModelException {
		validatePreparedStatement();

		try {
			return preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ModelException(friendlyMessage(e), e);
		} finally {
			close();
		}
	}

	public void createStatement() throws ModelException {
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao criar o statement", e);
		}
	}

	public void executeQuery(String sql) throws ModelException {
		validateStatement();

		try {
			resultSet = statement.executeQuery(sql);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao executar SQL", e);
		}
	}

	public void executeQuery() throws ModelException {
		validatePreparedStatement();

		try {
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao executar SQL", e);
		}
	}

	public boolean next() throws ModelException {
		validateResultSet();

		try {
			boolean hasRow = resultSet.next();

			// acabaram as linhas: libera a conexão agora, sem esperar o DAO
			if (!hasRow)
				close();

			return hasRow;
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao chamar próximo registro", e);
		}
	}

	public int getInt(String column) throws ModelException {
		try {
			return resultSet.getInt(column);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao chamar getInt", e);
		}
	}

	public double getDouble(String column) throws ModelException {
		try {
			return resultSet.getDouble(column);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao chamar getDouble", e);
		}
	}

	public boolean getBoolean(String column) throws ModelException {
		try {
			return resultSet.getBoolean(column);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao chamar getBoolean", e);
		}
	}

	public String getString(String column) throws ModelException {
		try {
			return resultSet.getString(column);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao chamar getString", e);
		}
	}

	public Date getDate(String column) throws ModelException {
		try {
			return resultSet.getDate(column);
		} catch (SQLException e) {
			close();
			throw new ModelException("Erro ao chamar getDate", e);
		}
	}

	/**
	 * Fecha ResultSet, statements e a conexão. Pode ser chamado várias vezes.
	 * Nos DAOs, chame no "finally" de consultas que não leem até o fim
	 * (por exemplo, findById).
	 */
	public void close() {
		if (closed)
			return;
		closed = true;

		try {
			if (resultSet != null)
				resultSet.close();
		} catch (SQLException ignored) {
		}
		try {
			if (preparedStatement != null)
				preparedStatement.close();
		} catch (SQLException ignored) {
		}
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException ignored) {
		}
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException ignored) {
		}
	}

	/**
	 * Padrão para LIKE: "ana" vira "%ana%". Os caracteres % e _ digitados pelo
	 * usuário são "escapados" para valerem como texto comum.
	 */
	public static String like(String term) {
		if (term == null)
			return "%";

		String escaped = term.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return "%" + escaped + "%";
	}

	// Traduz os códigos de erro mais comuns do MySQL/MariaDB para algo legível
	private static String friendlyMessage(SQLException e) {
		switch (e.getErrorCode()) {
		case 1451: // não dá para apagar: outra tabela aponta para este registro
			return "Existem registros vinculados a este item. Remova os vínculos antes de excluir.";
		case 1452: // chave estrangeira apontando para algo que não existe
			return "O registro relacionado informado não existe.";
		case 1062: // valor repetido em coluna UNIQUE
			return "Já existe um registro com esses dados.";
		case 3819: // CHECK violado (MySQL 8.0.16+)
		case 4025: // CHECK violado (MariaDB)
			return "Algum valor informado não é permitido. Confira áreas e datas.";
		default:
			return "Erro ao executar SQL";
		}
	}

	private void validatePreparedStatement() throws ModelException {
		if (preparedStatement == null)
			throw new ModelException("Preparede Statement não foi criado");
	}

	private void validateStatement() throws ModelException {
		if (statement == null)
			throw new ModelException("Statement não foi criado");
	}

	private void validateResultSet() throws ModelException {
		if (resultSet == null)
			throw new ModelException("Result set não foi criado");
	}
}
