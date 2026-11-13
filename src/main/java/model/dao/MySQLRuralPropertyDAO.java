package model.dao;

import java.util.ArrayList;
import java.util.List;

import model.ModelException;
import model.PropertyType;
import model.RuralProperty;
import model.User;

public class MySQLRuralPropertyDAO implements RuralPropertyDAO {

	// SELECT que já traz o dono (JOIN) e quantos plantios a propriedade tem
	// (subconsulta), tudo numa consulta só.
	private static final String SELECT = "SELECT rp.id, rp.name, rp.property_type, rp.city, rp.state, "
			+ " rp.area, rp.acquired_on, rp.irrigated, rp.map_url, rp.notes, rp.owner_id, "
			+ " u.nome AS owner_name, u.sexo AS owner_gender, u.email AS owner_email, "
			+ " (SELECT COUNT(*) FROM plantings pl WHERE pl.property_id = rp.id) AS planting_count "
			+ " FROM rural_properties rp "
			+ " INNER JOIN users u ON u.id = rp.owner_id ";

	private static final String SEARCH = " WHERE rp.name LIKE ? OR rp.city LIKE ? OR rp.state LIKE ? OR u.nome LIKE ? ";

	@Override
	public boolean save(RuralProperty property) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlInsert = "INSERT INTO rural_properties "
				+ " (name, property_type, city, state, area, acquired_on, irrigated, map_url, notes, owner_id) "
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		db.prepareStatement(sqlInsert);
		bindFields(db, property);

		return db.executeUpdate() > 0;
	}

	@Override
	public boolean update(RuralProperty property) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlUpdate = "UPDATE rural_properties "
				+ " SET name = ?, property_type = ?, city = ?, state = ?, area = ?, "
				+ " acquired_on = ?, irrigated = ?, map_url = ?, notes = ?, owner_id = ? "
				+ " WHERE id = ?;";

		db.prepareStatement(sqlUpdate);
		bindFields(db, property);
		db.setInt(11, property.getId());

		return db.executeUpdate() > 0;
	}

	// Os dez primeiros parâmetros são os mesmos no INSERT e no UPDATE
	private void bindFields(DBHandler db, RuralProperty p) throws ModelException {
		db.setString(1, p.getName());
		db.setString(2, p.getType().name());
		db.setString(3, p.getCity());
		db.setString(4, p.getState());
		db.setDouble(5, p.getArea());

		if (p.getAcquiredOn() == null)
			db.setNullDate(6);
		else
			db.setDate(6, p.getAcquiredOn());

		db.setBoolean(7, p.isIrrigated());
		db.setString(8, p.getMapUrl());
		db.setString(9, p.getNotes());
		db.setInt(10, p.getOwner().getId());
	}

	@Override
	public boolean delete(RuralProperty property) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlDelete = " DELETE FROM rural_properties "
				+ " WHERE id = ?;";

		db.prepareStatement(sqlDelete);
		db.setInt(1, property.getId());

		// Se existir plantio nesta propriedade, o MySQL recusa (chave estrangeira)
		// e o DBHandler devolve uma ModelException com mensagem amigável.
		return db.executeUpdate() > 0;
	}

	@Override
	public List<RuralProperty> listAll() throws ModelException {

		DBHandler db = new DBHandler();

		List<RuralProperty> properties = new ArrayList<RuralProperty>();

		String sqlQuery = SELECT + " ORDER BY rp.name, rp.id;";

		db.createStatement();
		db.executeQuery(sqlQuery);

		while (db.next()) {
			properties.add(createProperty(db));
		}

		return properties;
	}

	@Override
	public List<RuralProperty> listPage(String search, int limit, int offset) throws ModelException {

		DBHandler db = new DBHandler();

		List<RuralProperty> properties = new ArrayList<RuralProperty>();

		String sqlQuery = SELECT + SEARCH
				+ " ORDER BY rp.name, rp.id "
				+ " LIMIT ? OFFSET ?;";

		String pattern = DBHandler.like(search);

		db.prepareStatement(sqlQuery);
		db.setString(1, pattern);
		db.setString(2, pattern);
		db.setString(3, pattern);
		db.setString(4, pattern);
		db.setInt(5, limit);
		db.setInt(6, offset);
		db.executeQuery();

		while (db.next()) {
			properties.add(createProperty(db));
		}

		return properties;
	}

	@Override
	public int count(String search) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = "SELECT COUNT(*) AS total FROM rural_properties rp "
				+ " INNER JOIN users u ON u.id = rp.owner_id "
				+ SEARCH + ";";

		String pattern = DBHandler.like(search);

		try {
			db.prepareStatement(sql);
			db.setString(1, pattern);
			db.setString(2, pattern);
			db.setString(3, pattern);
			db.setString(4, pattern);
			db.executeQuery();

			return db.next() ? db.getInt("total") : 0;
		} finally {
			db.close();
		}
	}

	@Override
	public RuralProperty findById(int id) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = SELECT + " WHERE rp.id = ?;";

		try {
			db.prepareStatement(sql);
			db.setInt(1, id);
			db.executeQuery();

			return db.next() ? createProperty(db) : null;
		} finally {
			db.close();
		}
	}

	private RuralProperty createProperty(DBHandler db) throws ModelException {

		User owner = new User(db.getInt("owner_id"));
		owner.setName(db.getString("owner_name"));
		owner.setGender(db.getString("owner_gender"));
		owner.setEmail(db.getString("owner_email"));

		RuralProperty p = new RuralProperty(db.getInt("id"));
		p.setName(db.getString("name"));
		p.setType(PropertyType.fromCode(db.getString("property_type")));
		p.setCity(db.getString("city"));
		p.setState(db.getString("state"));
		p.setArea(db.getDouble("area"));
		p.setAcquiredOn(db.getDate("acquired_on"));
		p.setIrrigated(db.getBoolean("irrigated"));
		p.setMapUrl(db.getString("map_url"));
		p.setNotes(db.getString("notes"));
		p.setPlantingCount(db.getInt("planting_count"));
		p.setOwner(owner);

		return p;
	}
}
