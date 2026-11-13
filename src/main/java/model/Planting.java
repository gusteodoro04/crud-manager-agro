package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Plantio (uma lavoura/talhão plantado numa propriedade).
 * Relacionamentos: property (onde foi plantado) e manager (quem é o responsável).
 */
public class Planting {

	private int id;
	private String crop; // cultura: Café, Soja, Milho...
	private String variety; // cultivar / variedade
	private Date plantingDate;
	private Date expectedHarvest;
	private double area; // hectares plantados
	private double expectedYield; // sacas por hectare
	private PlantingStatus status;
	private String notes;
	private RuralProperty property;
	private User manager;

	public Planting() {
		this(0);
	}

	public Planting(int id) {
		this.id = id;
		this.status = PlantingStatus.PLANNED;
	}

	public int getId() {
		return id;
	}

	public String getCrop() {
		return crop;
	}

	public void setCrop(String crop) {
		this.crop = crop;
	}

	public String getVariety() {
		return variety;
	}

	public void setVariety(String variety) {
		this.variety = variety;
	}

	public Date getPlantingDate() {
		return plantingDate;
	}

	public void setPlantingDate(Date plantingDate) {
		this.plantingDate = plantingDate;
	}

	public Date getExpectedHarvest() {
		return expectedHarvest;
	}

	public void setExpectedHarvest(Date expectedHarvest) {
		this.expectedHarvest = expectedHarvest;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getExpectedYield() {
		return expectedYield;
	}

	public void setExpectedYield(double expectedYield) {
		this.expectedYield = expectedYield;
	}

	public PlantingStatus getStatus() {
		return status;
	}

	public void setStatus(PlantingStatus status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public RuralProperty getProperty() {
		return property;
	}

	public void setProperty(RuralProperty property) {
		this.property = property;
	}

	public User getManager() {
		return manager;
	}

	public void setManager(User manager) {
		this.manager = manager;
	}

	// ---------------------------------------------------------------
	// Valores calculados (não existem no banco)
	// ---------------------------------------------------------------

	/** Produção estimada em sacas = área plantada x produtividade esperada. */
	public double getEstimatedBags() {
		return area * expectedYield;
	}

	/** Quantos dias faltam para a colheita prevista (negativo = já passou). */
	public long getDaysToHarvest() {
		if (expectedHarvest == null)
			return 0;
		return ChronoUnit.DAYS.between(LocalDate.now(), DateUtil.toLocalDate(expectedHarvest));
	}

	/** Texto curto para a lista: "em 12 dias", "em 3 meses", "hoje", "há 5 dias". */
	public String getHarvestCountdown() {
		long days = getDaysToHarvest();

		if (days == 0)
			return "hoje";
		if (days == 1)
			return "amanhã";
		if (days == -1)
			return "ontem";
		if (days > 1 && days <= 45)
			return "em " + days + " dias";
		if (days > 45) {
			long months = Math.round(days / 30.4);
			return months == 1 ? "em 1 mês" : "em " + months + " meses";
		}
		// passado
		long past = -days;
		if (past <= 45)
			return "há " + past + " dias";
		long months = Math.round(past / 30.4);
		return months == 1 ? "há 1 mês" : "há " + months + " meses";
	}
}
