package model.dao;

import java.util.List;

import model.ModelException;
import model.RuralProperty;

/**
 * Acesso ao banco para o cadastro de Propriedades Rurais (atividade 3).
 */
public interface RuralPropertyDAO {
	boolean save(RuralProperty property) throws ModelException;
	boolean update(RuralProperty property) throws ModelException;
	boolean delete(RuralProperty property) throws ModelException;
	// Todas, em ordem alfabética: alimenta o <select> do formulário de plantio
	List<RuralProperty> listAll() throws ModelException;
	RuralProperty findById(int id) throws ModelException;

	// Paginação e busca (nome, cidade, UF ou dono)
	List<RuralProperty> listPage(String search, int limit, int offset) throws ModelException;
	int count(String search) throws ModelException;
}
