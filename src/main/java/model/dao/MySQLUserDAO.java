package model.dao;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import model.ModelException;
import model.User;

public class MySQLUserDAO implements UserDAO {

	@Override
	public boolean save(User user) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlInsert = "INSERT INTO users (nome, sexo, email, password_hash) "
				+ " VALUES (?, ?, ?, ?);";

		db.prepareStatement(sqlInsert);
		db.setString(1, user.getName());
		db.setString(2, user.getGender());
		db.setString(3, user.getEmail());
		db.setString(4, user.getPasswordHash());

		return db.executeUpdate() > 0;
	}

	@Override
	public boolean update(User user) throws ModelException {

		DBHandler db = new DBHandler();

		// A senha só é atualizada quando uma nova foi digitada (hash preenchido)
		boolean changePassword = user.getPasswordHash() != null;

		String sqlUpdate = "UPDATE users "
				         	+ "SET nome = ?, "
				         	+ "sexo = ?, "
				         	+ "email = ? "
				         	+ (changePassword ? ", password_hash = ? " : "")
				         + "WHERE id = ?";

		db.prepareStatement(sqlUpdate);

		int i = 1;
		db.setString(i++, user.getName());
		db.setString(i++, user.getGender());
		db.setString(i++, user.getEmail());
		if (changePassword)
			db.setString(i++, user.getPasswordHash());
		db.setInt(i, user.getId());

		return db.executeUpdate() > 0;
	}

	@Override
	public boolean delete(User user) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlDelete = " DELETE FROM users "
		                 + " WHERE id = ?;";

		db.prepareStatement(sqlDelete);
		db.setInt(1, user.getId());

		try {

			return db.executeUpdate() > 0;

		} catch (ModelException e) {
			// usuário com posts, empresas, propriedades ou plantios: não pode apagar
			if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
				return false;
			}

			throw e;
		}
	}

	@Override
	public List<User> listAll() throws ModelException {

		DBHandler db = new DBHandler();

		List<User> users = new ArrayList<User>();

		// Declara um instrução SQL
		String sqlQuery = "SELECT id, nome, sexo, email FROM users ORDER BY nome, id";

		db.createStatement();

		db.executeQuery(sqlQuery);

		while (db.next()) {
			User u = createUser(db);
			users.add(u);
		}

		return users;
	}

	@Override
	public List<User> listPage(String search, int limit, int offset) throws ModelException {

		DBHandler db = new DBHandler();

		List<User> users = new ArrayList<User>();

		String sqlQuery = "SELECT id, nome, sexo, email FROM users "
				+ "WHERE nome LIKE ? OR email LIKE ? "
				+ "ORDER BY nome, id "
				+ "LIMIT ? OFFSET ?;";

		String pattern = DBHandler.like(search);

		db.prepareStatement(sqlQuery);
		db.setString(1, pattern);
		db.setString(2, pattern);
		db.setInt(3, limit);
		db.setInt(4, offset);
		db.executeQuery();

		while (db.next()) {
			users.add(createUser(db));
		}

		return users;
	}

	@Override
	public int count(String search) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = "SELECT COUNT(*) AS total FROM users WHERE nome LIKE ? OR email LIKE ?;";
		String pattern = DBHandler.like(search);

		try {
			db.prepareStatement(sql);
			db.setString(1, pattern);
			db.setString(2, pattern);
			db.executeQuery();

			return db.next() ? db.getInt("total") : 0;
		} finally {
			db.close();
		}
	}

	@Override
	public User findById(int id) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = "SELECT id, nome, sexo, email FROM users WHERE id = ?";

		try {
			db.prepareStatement(sql);
			db.setInt(1, id);
			db.executeQuery();

			return db.next() ? createUser(db) : null;
		} finally {
			db.close();
		}
	}

	@Override
	public User findByEmail(String email) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = "SELECT id, nome, sexo, email, password_hash FROM users WHERE email = ? LIMIT 1";

		try {
			db.prepareStatement(sql);
			db.setString(1, email);
			db.executeQuery();

			if (!db.next())
				return null;

			User u = createUser(db);
			u.setPasswordHash(db.getString("password_hash"));
			return u;
		} finally {
			db.close();
		}
	}

	private User createUser(DBHandler db) throws ModelException {
		User u = new User(db.getInt("id"));
		u.setName(db.getString("nome"));
		u.setGender(db.getString("sexo"));
		u.setEmail(db.getString("email"));

		return u;
	}
}
