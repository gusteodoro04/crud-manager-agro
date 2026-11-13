package controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import model.DateUtil;
import model.Planting;

/**
 * Dados do "calendário da safra" do painel: uma linha do tempo de 1º de julho a 30 de
 * junho (a safra agrícola brasileira vai de julho a junho), com uma linha por propriedade,
 * uma barra por plantio e um marcador de "hoje".
 */
public class SeasonCalendar {

	private static final String[] MONTHS = { "jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out",
			"nov", "dez" };

	/** Um mês da régua de cima do calendário. */
	public static class MonthMark {
		private final String label;
		private final double left;
		private final double width;
		private final boolean current;

		MonthMark(String label, double left, double width, boolean current) {
			this.label = label;
			this.left = left;
			this.width = width;
			this.current = current;
		}

		public String getLabel() {
			return label;
		}

		public String getLeftCss() {
			return String.format(Locale.ROOT, "%.2f", left);
		}

		public String getWidthCss() {
			return String.format(Locale.ROOT, "%.2f", width);
		}

		public boolean isCurrent() {
			return current;
		}
	}

	private final LocalDate start;
	private final LocalDate end;
	private final long totalDays;
	private final double today;
	private final List<MonthMark> months = new ArrayList<MonthMark>();
	private final List<CalendarRow> rows = new ArrayList<CalendarRow>();
	private final boolean truncated;

	/**
	 * @param plantings plantios que encostam na safra, já ordenados por propriedade e data
	 * @param truncated true se a consulta foi cortada por ter plantios demais (a tela avisa)
	 */
	public SeasonCalendar(LocalDate todayDate, List<Planting> plantings, boolean truncated) {
		this.truncated = truncated;
		this.start = startOf(todayDate);
		this.end = endOf(todayDate);
		this.totalDays = ChronoUnit.DAYS.between(start, end) + 1;
		this.today = position(todayDate);

		// régua de meses
		LocalDate month = start;
		while (!month.isAfter(end)) {
			double left = position(month);
			double width = month.lengthOfMonth() * 100.0 / totalDays;
			boolean current = month.getYear() == todayDate.getYear() && month.getMonthValue() == todayDate.getMonthValue();

			months.add(new MonthMark(MONTHS[month.getMonthValue() - 1], left, width, current));
			month = month.plusMonths(1);
		}

		// uma linha por propriedade, uma barra por plantio
		if (plantings != null) {
			Map<Integer, CalendarRow> byProperty = new LinkedHashMap<Integer, CalendarRow>();
			Map<Integer, List<LocalDate>> laneEnds = new LinkedHashMap<Integer, List<LocalDate>>();

			for (Planting p : plantings) {
				LocalDate from = DateUtil.toLocalDate(p.getPlantingDate());
				LocalDate to = DateUtil.toLocalDate(p.getExpectedHarvest());
				boolean startsBefore = from.isBefore(start);
				boolean endsAfter = to.isAfter(end);
				double left = position(startsBefore ? start : from);
				// +1 dia: o dia da colheita também é ocupado pela barra
				double right = position((endsAfter ? end : to).plusDays(1));
				// plantios curtos continuam visíveis
				double width = Math.max(right - left, 0.8);

				int propertyId = p.getProperty().getId();
				CalendarRow row = byProperty.get(propertyId);
				List<LocalDate> ends = laneEnds.get(propertyId);

				if (row == null) {
					row = new CalendarRow(p.getProperty());
					ends = new ArrayList<LocalDate>();
					byProperty.put(propertyId, row);
					laneEnds.put(propertyId, ends);
					rows.add(row);
				}

				// primeira faixa que já terminou antes de este plantio começar; se não houver, abre outra
				int lane = 0;
				while (lane < ends.size() && !ends.get(lane).isBefore(from))
					lane++;
				if (lane == ends.size())
					ends.add(to);
				else
					ends.set(lane, to);

				row.add(new CalendarBar(p, left, width, startsBefore, endsAfter, lane));
			}
		}
	}

	/** 1º de julho do ano em que a safra atual começou. */
	public static LocalDate startOf(LocalDate today) {
		int year = today.getMonthValue() >= 7 ? today.getYear() : today.getYear() - 1;
		return LocalDate.of(year, 7, 1);
	}

	/** 30 de junho do ano seguinte ao do início da safra. */
	public static LocalDate endOf(LocalDate today) {
		return startOf(today).plusYears(1).minusDays(1);
	}

	// posição (0 a 100 %) de uma data dentro da linha do tempo
	private double position(LocalDate date) {
		double days = ChronoUnit.DAYS.between(start, date);
		return Math.max(0, Math.min(100, days * 100.0 / totalDays));
	}

	/** "2026/27" */
	public String getLabel() {
		return start.getYear() + "/" + String.format("%02d", (start.getYear() + 1) % 100);
	}

	public List<MonthMark> getMonths() {
		return months;
	}

	public List<CalendarRow> getRows() {
		return rows;
	}

	/** Há plantios da safra que não couberam no calendário. */
	public boolean isTruncated() {
		return truncated;
	}

	public String getTodayCss() {
		return String.format(Locale.ROOT, "%.2f", today);
	}
}
