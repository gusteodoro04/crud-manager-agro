package model;

/**
 * Linha do resumo "área por cultura" do painel: quantos plantios e quantos
 * hectares de uma mesma cultura (soma feita pelo banco com GROUP BY).
 */
public class CropArea {

	private final String crop;
	private final int plantingCount;
	private final double area;

	// largura da barra no painel (0 a 100), calculada pelo controller em relação à maior cultura
	private double percent;

	public CropArea(String crop, int plantingCount, double area) {
		this.crop = crop;
		this.plantingCount = plantingCount;
		this.area = area;
	}

	public String getCrop() {
		return crop;
	}

	public int getPlantingCount() {
		return plantingCount;
	}

	public double getArea() {
		return area;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	/** Largura em % já no formato do CSS (ponto decimal, independente do idioma do servidor). */
	public String getPercentCss() {
		return String.format(java.util.Locale.ROOT, "%.1f", percent);
	}
}
