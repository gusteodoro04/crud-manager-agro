package model.dao;

import java.util.Date;
import java.util.List;

import model.CropArea;
import model.DashboardSummary;
import model.ModelException;
import model.Planting;

/**
 * Consultas só de leitura que alimentam o painel inicial.
 * "today" vem do Java (e não de CURDATE()) para o painel e os textos
 * "em 12 dias" concordarem sobre qual é o dia de hoje.
 */
public interface DashboardDAO {

	// Contadores gerais (propriedades, plantios por status, hectares, sacas...)
	DashboardSummary summary(Date today) throws ModelException;

	// Plantios em desenvolvimento, do que vai ser colhido primeiro para o último
	List<Planting> nextHarvests(int limit) throws ModelException;

	// Plantios que "encostam" no período [from, to]: alimenta o calendário da safra
	List<Planting> timeline(Date from, Date to, int limit) throws ModelException;

	// Hectares por cultura entre os plantios planejados e em desenvolvimento
	List<CropArea> areaByCrop(int limit) throws ModelException;
}
