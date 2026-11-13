package model.dao;

import java.util.List;
import java.util.Map;

import model.ModelException;
import model.Planting;
import model.PlantingStatus;

/**
 * Acesso ao banco para o cadastro de Plantios (atividade 3).
 */
public interface PlantingDAO {
	boolean save(Planting planting) throws ModelException;
	boolean update(Planting planting) throws ModelException;
	boolean delete(Planting planting) throws ModelException;
	List<Planting> listAll() throws ModelException;
	Planting findById(int id) throws ModelException;

	// Paginação e busca (cultura, variedade, propriedade ou responsável).
	// status == null significa "todos os status".
	List<Planting> listPage(String search, PlantingStatus status, int limit, int offset) throws ModelException;
	int count(String search, PlantingStatus status) throws ModelException;

	// Quantos plantios há em cada status para a mesma busca: alimenta as abas
	// "Todos (29) | Planejado (6) | ..." da tela de plantios.
	Map<PlantingStatus, Integer> countByStatus(String search) throws ModelException;
}
