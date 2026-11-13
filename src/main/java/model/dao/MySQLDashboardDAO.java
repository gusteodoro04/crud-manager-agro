package model.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.CropArea;
import model.DashboardSummary;
import model.ModelException;
import model.Planting;

public class MySQLDashboardDAO implements DashboardDAO {

	@Override
	public DashboardSummary summary(Date today) throws ModelException {

		DashboardSummary summary = new DashboardSummary();

		// Daqui a 30 dias (limite do "colheita próxima")
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_MONTH, 30);
		Date in30Days = cal.getTime();

		// 1) propriedades
		DBHandler db = new DBHandler();
		try {
			db.prepareStatement("SELECT COUNT(*) AS total, COALESCE(SUM(area), 0) AS total_area "
					+ " FROM rural_properties;");
			db.executeQuery();

			if (db.next()) {
				summary.setPropertyCount(db.getInt("total"));
				summary.setPropertyArea(db.getDouble("total_area"));
			}
		} finally {
			db.close();
		}

		// 2) plantios: uma única consulta calcula todos os contadores
		db = new DBHandler();
		try {
			String sql = "SELECT COUNT(*) AS total, "
					+ " COUNT(CASE WHEN status = 'PLANNED' THEN 1 END) AS planned, "
					+ " COUNT(CASE WHEN status = 'GROWING' THEN 1 END) AS growing, "
					+ " COUNT(CASE WHEN status = 'HARVESTED' THEN 1 END) AS harvested, "
					+ " COUNT(CASE WHEN status = 'LOST' THEN 1 END) AS lost, "
					+ " COALESCE(SUM(CASE WHEN status = 'GROWING' THEN area END), 0) AS growing_area, "
					+ " COALESCE(SUM(CASE WHEN status = 'GROWING' THEN area * expected_yield END), 0) AS growing_bags, "
					+ " COUNT(CASE WHEN status = 'GROWING' AND expected_harvest BETWEEN ? AND ? THEN 1 END) AS soon, "
					+ " COUNT(CASE WHEN status = 'GROWING' AND expected_harvest < ? THEN 1 END) AS overdue "
					+ " FROM plantings;";

			db.prepareStatement(sql);
			db.setDate(1, today);
			db.setDate(2, in30Days);
			db.setDate(3, today);
			db.executeQuery();

			if (db.next()) {
				summary.setPlantingCount(db.getInt("total"));
				summary.setPlannedCount(db.getInt("planned"));
				summary.setGrowingCount(db.getInt("growing"));
				summary.setHarvestedCount(db.getInt("harvested"));
				summary.setLostCount(db.getInt("lost"));
				summary.setGrowingArea(db.getDouble("growing_area"));
				summary.setGrowingBags(db.getDouble("growing_bags"));
				summary.setHarvestSoonCount(db.getInt("soon"));
				summary.setOverdueCount(db.getInt("overdue"));
			}
		} finally {
			db.close();
		}

		return summary;
	}

	@Override
	public List<Planting> nextHarvests(int limit) throws ModelException {

		DBHandler db = new DBHandler();

		List<Planting> plantings = new ArrayList<Planting>();

		String sql = MySQLPlantingDAO.SELECT
				+ " WHERE pl.status = 'GROWING' "
				+ " ORDER BY pl.expected_harvest, pl.id "
				+ " LIMIT ?;";

		db.prepareStatement(sql);
		db.setInt(1, limit);
		db.executeQuery();

		while (db.next()) {
			plantings.add(MySQLPlantingDAO.createPlanting(db));
		}

		return plantings;
	}

	@Override
	public List<Planting> timeline(Date from, Date to, int limit) throws ModelException {

		DBHandler db = new DBHandler();

		List<Planting> plantings = new ArrayList<Planting>();

		// Dois períodos se encostam quando um começa antes de o outro terminar
		// e termina depois de o outro começar.
		String sql = MySQLPlantingDAO.SELECT
				+ " WHERE pl.planting_date <= ? AND pl.expected_harvest >= ? "
				+ " ORDER BY rp.name, rp.id, pl.planting_date, pl.id "
				+ " LIMIT ?;";

		db.prepareStatement(sql);
		db.setDate(1, to);
		db.setDate(2, from);
		db.setInt(3, limit);
		db.executeQuery();

		while (db.next()) {
			plantings.add(MySQLPlantingDAO.createPlanting(db));
		}

		return plantings;
	}

	@Override
	public List<CropArea> areaByCrop(int limit) throws ModelException {

		DBHandler db = new DBHandler();

		List<CropArea> crops = new ArrayList<CropArea>();

		String sql = "SELECT crop, COUNT(*) AS total, SUM(area) AS total_area "
				+ " FROM plantings "
				+ " WHERE status IN ('PLANNED', 'GROWING') "
				+ " GROUP BY crop "
				+ " ORDER BY total_area DESC, crop "
				+ " LIMIT ?;";

		db.prepareStatement(sql);
		db.setInt(1, limit);
		db.executeQuery();

		while (db.next()) {
			crops.add(new CropArea(db.getString("crop"), db.getInt("total"), db.getDouble("total_area")));
		}

		return crops;
	}
}
