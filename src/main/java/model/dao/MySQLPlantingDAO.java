package model.dao;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import model.ModelException;
import model.Planting;
import model.PlantingStatus;
import model.RuralProperty;
import model.User;

public class MySQLPlantingDAO implements PlantingDAO {

	// SELECT com a propriedade e o responsável já incluídos (dois JOINs).
	// Fica "package-private" porque o MySQLDashboardDAO reaproveita.
	static final String SELECT = "SELECT pl.id, pl.crop, pl.variety, pl.planting_date, pl.expected_harvest, "
			+ " pl.area, pl.expected_yield, pl.status, pl.notes, "
			+ " pl.property_id, rp.name AS property_name, rp.city AS property_city, rp.state AS property_state, "
			+ " pl.manager_id, u.nome AS manager_name, u.sexo AS manager_gender, u.email AS manager_email "
			+ " FROM plantings pl "
			+ " INNER JOIN rural_properties rp ON rp.id = pl.property_id "
			+ " INNER JOIN users u ON u.id = pl.manager_id ";

	// Mesmos JOINs, sem as colunas (para o COUNT)
	private static final String FROM = " FROM plantings pl "
			+ " INNER JOIN rural_properties rp ON rp.id = pl.property_id "
			+ " INNER JOIN users u ON u.id = pl.manager_id ";

	private static final String SEARCH = " (pl.crop LIKE ? OR pl.variety LIKE ? OR rp.name LIKE ? OR u.nome LIKE ?) ";

	@Override
	public boolean save(Planting planting) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlInsert = "INSERT INTO plantings "
				+ " (crop, variety, planting_date, expected_harvest, area, expected_yield, status, notes, property_id, manager_id) "
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		db.prepareStatement(sqlInsert);
		bindFields(db, planting);

		return db.executeUpdate() > 0;
	}

	@Override
	public boolean update(Planting planting) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlUpdate = "UPDATE plantings "
				+ " SET crop = ?, variety = ?, planting_date = ?, expected_harvest = ?, area = ?, "
				+ " expected_yield = ?, status = ?, notes = ?, property_id = ?, manager_id = ? "
				+ " WHERE id = ?;";

		db.prepareStatement(sqlUpdate);
		bindFields(db, planting);
		db.setInt(11, planting.getId());

		return db.executeUpdate() > 0;
	}

	// Os dez primeiros parâmetros são os mesmos no INSERT e no UPDATE
	private void bindFields(DBHandler db, Planting p) throws ModelException {
		db.setString(1, p.getCrop());
		db.setString(2, p.getVariety());
		db.setDate(3, p.getPlantingDate());
		db.setDate(4, p.getExpectedHarvest());
		db.setDouble(5, p.getArea());
		db.setDouble(6, p.getExpectedYield());
		db.setString(7, p.getStatus().name());
		db.setString(8, p.getNotes());
		db.setInt(9, p.getProperty().getId());
		db.setInt(10, p.getManager().getId());
	}

	@Override
	public boolean delete(Planting planting) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlDelete = " DELETE FROM plantings "
				+ " WHERE id = ?;";

		db.prepareStatement(sqlDelete);
		db.setInt(1, planting.getId());

		return db.executeUpdate() > 0;
	}

	@Override
	public List<Planting> listAll() throws ModelException {

		DBHandler db = new DBHandler();

		List<Planting> plantings = new ArrayList<Planting>();

		String sqlQuery = SELECT + " ORDER BY pl.planting_date DESC, pl.id DESC;";

		db.createStatement();
		db.executeQuery(sqlQuery);

		while (db.next()) {
			plantings.add(createPlanting(db));
		}

		return plantings;
	}

	@Override
	public List<Planting> listPage(String search, PlantingStatus status, int limit, int offset)
			throws ModelException {

		DBHandler db = new DBHandler();

		List<Planting> plantings = new ArrayList<Planting>();

		String sqlQuery = SELECT
				+ " WHERE " + SEARCH + (status != null ? " AND pl.status = ? " : "")
				+ " ORDER BY pl.planting_date DESC, pl.id DESC "
				+ " LIMIT ? OFFSET ?;";

		String pattern = DBHandler.like(search);

		db.prepareStatement(sqlQuery);

		int i = 1;
		db.setString(i++, pattern);
		db.setString(i++, pattern);
		db.setString(i++, pattern);
		db.setString(i++, pattern);
		if (status != null)
			db.setString(i++, status.name());
		db.setInt(i++, limit);
		db.setInt(i, offset);
		db.executeQuery();

		while (db.next()) {
			plantings.add(createPlanting(db));
		}

		return plantings;
	}

	@Override
	public int count(String search, PlantingStatus status) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = "SELECT COUNT(*) AS total " + FROM
				+ " WHERE " + SEARCH + (status != null ? " AND pl.status = ? " : "") + ";";

		String pattern = DBHandler.like(search);

		try {
			db.prepareStatement(sql);

			int i = 1;
			db.setString(i++, pattern);
			db.setString(i++, pattern);
			db.setString(i++, pattern);
			db.setString(i++, pattern);
			if (status != null)
				db.setString(i, status.name());
			db.executeQuery();

			return db.next() ? db.getInt("total") : 0;
		} finally {
			db.close();
		}
	}

	@Override
	public Map<PlantingStatus, Integer> countByStatus(String search) throws ModelException {

		DBHandler db = new DBHandler();

		// Começa com zero em todos os status (status sem plantios não vêm no GROUP BY)
		Map<PlantingStatus, Integer> counts = new EnumMap<PlantingStatus, Integer>(PlantingStatus.class);
		for (PlantingStatus s : PlantingStatus.values())
			counts.put(s, 0);

		String sql = "SELECT pl.status AS status, COUNT(*) AS total " + FROM
				+ " WHERE " + SEARCH
				+ " GROUP BY pl.status;";

		String pattern = DBHandler.like(search);

		try {
			db.prepareStatement(sql);
			db.setString(1, pattern);
			db.setString(2, pattern);
			db.setString(3, pattern);
			db.setString(4, pattern);
			db.executeQuery();

			while (db.next()) {
				PlantingStatus s = PlantingStatus.fromCode(db.getString("status"));
				if (s != null)
					counts.put(s, db.getInt("total"));
			}
		} finally {
			db.close();
		}

		return counts;
	}

	@Override
	public Planting findById(int id) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = SELECT + " WHERE pl.id = ?;";

		try {
			db.prepareStatement(sql);
			db.setInt(1, id);
			db.executeQuery();

			return db.next() ? createPlanting(db) : null;
		} finally {
			db.close();
		}
	}

	// Monta um Planting (com propriedade e responsável) a partir da linha atual
	static Planting createPlanting(DBHandler db) throws ModelException {

		RuralProperty property = new RuralProperty(db.getInt("property_id"));
		property.setName(db.getString("property_name"));
		property.setCity(db.getString("property_city"));
		property.setState(db.getString("property_state"));

		User manager = new User(db.getInt("manager_id"));
		manager.setName(db.getString("manager_name"));
		manager.setGender(db.getString("manager_gender"));
		manager.setEmail(db.getString("manager_email"));

		Planting p = new Planting(db.getInt("id"));
		p.setCrop(db.getString("crop"));
		p.setVariety(db.getString("variety"));
		p.setPlantingDate(db.getDate("planting_date"));
		p.setExpectedHarvest(db.getDate("expected_harvest"));
		p.setArea(db.getDouble("area"));
		p.setExpectedYield(db.getDouble("expected_yield"));
		p.setStatus(PlantingStatus.fromCode(db.getString("status")));
		p.setNotes(db.getString("notes"));
		p.setProperty(property);
		p.setManager(manager);

		return p;
	}
}
