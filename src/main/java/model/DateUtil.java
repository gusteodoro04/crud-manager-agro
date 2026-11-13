package model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Conversões entre java.util.Date (usado no modelo, como no resto do projeto)
 * e java.time.LocalDate (mais fácil para fazer contas com datas).
 *
 * Importante: o JDBC devolve java.sql.Date, e java.sql.Date.toInstant() lança
 * exceção. Por isso usamos getTime() (milissegundos), que funciona nos dois.
 */
public final class DateUtil {

	private DateUtil() {
	}

	public static LocalDate toLocalDate(Date date) {
		if (date == null)
			return null;
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static Date toDate(LocalDate localDate) {
		if (localDate == null)
			return null;
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
