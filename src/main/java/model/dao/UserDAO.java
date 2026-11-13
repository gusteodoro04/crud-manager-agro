package model.dao;

import java.util.List;

import model.ModelException;
import model.User;

public interface UserDAO {
	boolean save(User user) throws ModelException ;
	boolean update(User user) throws ModelException;
	boolean delete(User user) throws ModelException;
	List<User> listAll() throws ModelException;
	User findById(int id) throws ModelException;

	// --- Atividade 3 ---------------------------------------------------
	// Paginação: uma "página" de usuários (LIMIT/OFFSET) filtrada por busca
	List<User> listPage(String search, int limit, int offset) throws ModelException;
	// Quantos usuários existem para a mesma busca (para calcular o nº de páginas)
	int count(String search) throws ModelException;
	// Login: devolve o usuário JÁ com o hash da senha preenchido (ou null)
	User findByEmail(String email) throws ModelException;
}
