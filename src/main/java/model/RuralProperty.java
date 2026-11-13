package model;

import java.util.Date;

/**
 * Propriedade rural (fazenda, sítio, chácara ou granja).
 * Relacionamento: cada propriedade pertence a um usuário (owner).
 */
public class RuralProperty {

	private int id;
	private String name;
	private PropertyType type;
	private String city;
	private String state; // UF, 2 letras
	private double area; // hectares
	private Date acquiredOn;
	private boolean irrigated;
	private String mapUrl;
	private String notes;
	private User owner;

	// Calculado pelo DAO só para as listas (quantos plantios usam esta propriedade)
	private int plantingCount;

	public RuralProperty() {
		this(0);
	}

	public RuralProperty(int id) {
		this.id = id;
		this.type = PropertyType.FAZENDA;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PropertyType getType() {
		return type;
	}

	public void setType(PropertyType type) {
		this.type = type;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public Date getAcquiredOn() {
		return acquiredOn;
	}

	public void setAcquiredOn(Date acquiredOn) {
		this.acquiredOn = acquiredOn;
	}

	public boolean isIrrigated() {
		return irrigated;
	}

	public void setIrrigated(boolean irrigated) {
		this.irrigated = irrigated;
	}

	public String getMapUrl() {
		return mapUrl;
	}

	public void setMapUrl(String mapUrl) {
		this.mapUrl = mapUrl;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public int getPlantingCount() {
		return plantingCount;
	}

	public void setPlantingCount(int plantingCount) {
		this.plantingCount = plantingCount;
	}

	/** "Poço Fundo, MG" */
	public String getLocation() {
		if (city == null || city.isEmpty())
			return state == null ? "" : state;
		return state == null || state.isEmpty() ? city : city + ", " + state;
	}
}
