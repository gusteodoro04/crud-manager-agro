package model;

/**
 * Números do painel inicial. Não é uma tabela: o DashboardDAO calcula tudo
 * com consultas de agregação (COUNT / SUM) e entrega pronto para a tela.
 */
public class DashboardSummary {

	private int propertyCount;
	private double propertyArea; // hectares somados de todas as propriedades

	private int plantingCount;
	private int plannedCount;
	private int growingCount;
	private int harvestedCount;
	private int lostCount;

	private double growingArea; // hectares com plantio em desenvolvimento
	private double growingBags; // produção estimada (sacas) dos plantios em desenvolvimento
	private int harvestSoonCount; // em desenvolvimento, colheita prevista nos próximos 30 dias
	private int overdueCount; // em desenvolvimento, mas a data de colheita já passou

	public int getPropertyCount() {
		return propertyCount;
	}

	public void setPropertyCount(int propertyCount) {
		this.propertyCount = propertyCount;
	}

	public double getPropertyArea() {
		return propertyArea;
	}

	public void setPropertyArea(double propertyArea) {
		this.propertyArea = propertyArea;
	}

	public int getPlantingCount() {
		return plantingCount;
	}

	public void setPlantingCount(int plantingCount) {
		this.plantingCount = plantingCount;
	}

	public int getPlannedCount() {
		return plannedCount;
	}

	public void setPlannedCount(int plannedCount) {
		this.plannedCount = plannedCount;
	}

	public int getGrowingCount() {
		return growingCount;
	}

	public void setGrowingCount(int growingCount) {
		this.growingCount = growingCount;
	}

	public int getHarvestedCount() {
		return harvestedCount;
	}

	public void setHarvestedCount(int harvestedCount) {
		this.harvestedCount = harvestedCount;
	}

	public int getLostCount() {
		return lostCount;
	}

	public void setLostCount(int lostCount) {
		this.lostCount = lostCount;
	}

	public double getGrowingArea() {
		return growingArea;
	}

	public void setGrowingArea(double growingArea) {
		this.growingArea = growingArea;
	}

	public double getGrowingBags() {
		return growingBags;
	}

	public void setGrowingBags(double growingBags) {
		this.growingBags = growingBags;
	}

	public int getHarvestSoonCount() {
		return harvestSoonCount;
	}

	public void setHarvestSoonCount(int harvestSoonCount) {
		this.harvestSoonCount = harvestSoonCount;
	}

	public int getOverdueCount() {
		return overdueCount;
	}

	public void setOverdueCount(int overdueCount) {
		this.overdueCount = overdueCount;
	}
}
