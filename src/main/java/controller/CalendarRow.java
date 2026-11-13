package controller;

import java.util.ArrayList;
import java.util.List;

import model.RuralProperty;

/**
 * Uma linha do "calendário da safra": uma propriedade e as barras dos plantios dela.
 * Plantios que acontecem ao mesmo tempo na mesma propriedade ficam em faixas (lanes)
 * diferentes, uma embaixo da outra, para as barras não se sobreporem.
 */
public class CalendarRow {

	private final RuralProperty property;
	private final List<CalendarBar> bars = new ArrayList<CalendarBar>();
	private int laneCount = 1;

	public CalendarRow(RuralProperty property) {
		this.property = property;
	}

	void add(CalendarBar bar) {
		bars.add(bar);
		laneCount = Math.max(laneCount, bar.getLane() + 1);
	}

	public RuralProperty getProperty() {
		return property;
	}

	public List<CalendarBar> getBars() {
		return bars;
	}

	/** Quantas faixas a linha precisa (a altura da linha na tela depende disso). */
	public int getLaneCount() {
		return laneCount;
	}
}
