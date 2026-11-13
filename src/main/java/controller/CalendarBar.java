package controller;

import java.util.Locale;

import model.Planting;

/**
 * Uma barra do "calendário da safra" do painel: onde o plantio começa e termina,
 * em porcentagem da largura da linha do tempo.
 */
public class CalendarBar {

	private final Planting planting;
	private final double left; // % a partir do começo da safra
	private final double width; // % da largura total
	private final boolean startsBefore; // o plantio começou antes desta safra
	private final boolean endsAfter; // a colheita prevista fica depois desta safra
	private final int lane; // faixa dentro da linha da propriedade (0 = a de cima)

	public CalendarBar(Planting planting, double left, double width, boolean startsBefore, boolean endsAfter,
			int lane) {
		this.planting = planting;
		this.left = left;
		this.width = width;
		this.startsBefore = startsBefore;
		this.endsAfter = endsAfter;
		this.lane = lane;
	}

	public Planting getPlanting() {
		return planting;
	}

	public double getLeft() {
		return left;
	}

	public double getWidth() {
		return width;
	}

	public boolean isStartsBefore() {
		return startsBefore;
	}

	public boolean isEndsAfter() {
		return endsAfter;
	}

	public int getLane() {
		return lane;
	}

	// O CSS exige ponto como separador decimal. Por isso formatamos aqui, com Locale.ROOT,
	// e não na página (onde o idioma pt-BR trocaria o ponto por vírgula).
	public String getLeftCss() {
		return String.format(Locale.ROOT, "%.2f", left);
	}

	public String getWidthCss() {
		return String.format(Locale.ROOT, "%.2f", width);
	}
}
