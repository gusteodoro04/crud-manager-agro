package model.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Company;
import model.ModelException;
import model.User;

public class MySQLCompanyDAO implements CompanyDAO {

	// SELECT com os dados do usuário já incluídos (um JOIN só)
	private static final String SELECT = " SELECT c.id AS company_id, c.`name`, c.`role`, c.`start`, c.`end`, "
			+ " c.user_id, u.nome, u.sexo, u.email \n"
			+ " FROM companies c \n"
			+ " INNER JOIN users u \n"
			+ " ON c.user_id = u.id \n";

	@Override
	public boolean save(Company company) throws ModelException {
		DBHandler db = new DBHandler();

		String sqlInsert = "INSERT INTO companies VALUES (DEFAULT, ?, ?, ?, ?, ?);";

		db.prepareStatement(sqlInsert);

		db.setString(1, company.getName());
		db.setString(2, company.getRole());
		db.setDate(3, company.getStart() == null ? new Date() : company.getStart());

		if (company.getEnd() == null)
			db.setNullDate(4);
		else db.setDate(4, company.getEnd());

		db.setInt(5, company.getUser().getId());

		return db.executeUpdate() > 0;
	}

	@Override
	public boolean update(Company company) throws ModelException {
		DBHandler db = new DBHandler();

		String sqlUpdate = "UPDATE companies "
				+ " SET name = ?, "
				+ " role = ?, "
				+ " start = ?, "
				+ " end = ?, "
				+ " user_id = ? "
				+ " WHERE id = ?; ";

		db.prepareStatement(sqlUpdate);

		db.setString(1, company.getName());
		db.setString(2, company.getRole());

		db.setDate(3, company.getStart() == null ? new Date() : company.getStart());

		if (company.getEnd() == null)
			db.setNullDate(4);
		else db.setDate(4, company.getEnd());

		db.setInt(5, company.getUser().getId());
		db.setInt(6, company.getId());

		return db.executeUpdate() > 0;
	}

	@Override
	public boolean delete(Company company) throws ModelException {
		DBHandler db = new DBHandler();

		String sqlDelete = " DELETE FROM companies "
		         + " WHERE id = ?;";

		db.prepareStatement(sqlDelete);
		db.setInt(1, company.getId());

		return db.executeUpdate() > 0;
	}

	@Override
	public List<Company> listAll() throws ModelException {
		DBHandler db = new DBHandler();

		List<Company> companies = new ArrayList<Company>();

		// Declara uma instrução SQL
		String sqlQuery = SELECT + " ORDER BY c.`name`, c.id;";

		db.createStatement();

		db.executeQuery(sqlQuery);

		while (db.next()) {
			companies.add(createCompany(db));
		}

		return companies;
	}

	@Override
	public List<Company> listPage(String search, int limit, int offset) throws ModelException {
		DBHandler db = new DBHandler();

		List<Company> companies = new ArrayList<Company>();

		String sqlQuery = SELECT
				+ " WHERE c.`name` LIKE ? OR c.`role` LIKE ? OR u.nome LIKE ? \n"
				+ " ORDER BY c.`name`, c.id \n"
				+ " LIMIT ? OFFSET ?;";

		String pattern = DBHandler.like(search);

		db.prepareStatement(sqlQuery);
		db.setString(1, pattern);
		db.setString(2, pattern);
		db.setString(3, pattern);
		db.setInt(4, limit);
		db.setInt(5, offset);
		db.executeQuery();

		while (db.next()) {
			companies.add(createCompany(db));
		}

		return companies;
	}

	@Override
	public int count(String search) throws ModelException {
		DBHandler db = new DBHandler();

		String sql = "SELECT COUNT(*) AS total FROM companies c "
				+ " INNER JOIN users u ON u.id = c.user_id "
				+ " WHERE c.`name` LIKE ? OR c.`role` LIKE ? OR u.nome LIKE ?;";
		String pattern = DBHandler.like(search);

		try {
			db.prepareStatement(sql);
			db.setString(1, pattern);
			db.setString(2, pattern);
			db.setString(3, pattern);
			db.executeQuery();

			return db.next() ? db.getInt("total") : 0;
		} finally {
			db.close();
		}
	}

	@Override
	public Company findById(int id) throws ModelException {
		DBHandler db = new DBHandler();

		String sql = SELECT + " WHERE c.id = ?;";

		try {
			db.prepareStatement(sql);
			db.setInt(1, id);
			db.executeQuery();

			return db.next() ? createCompany(db) : null;
		} finally {
			db.close();
		}
	}

	private Company createCompany(DBHandler db) throws ModelException {
		User user = new User(db.getInt("user_id"));
		user.setName(db.getString("nome"));
		user.setGender(db.getString("sexo"));
		user.setEmail(db.getString("email"));

		Company company = new Company(db.getInt("company_id"));
		company.setName(db.getString("name"));
		company.setRole(db.getString("role"));
		company.setStart(db.getDate("start"));
		company.setEnd(db.getDate("end"));
		company.setUser(user);

		return company;
	}
}
