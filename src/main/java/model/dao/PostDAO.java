package model.dao;

import java.util.List;

import model.ModelException;
import model.Post;

public interface PostDAO {
	boolean save(Post post) throws ModelException;
	boolean update(Post post) throws ModelException;
	boolean delete(Post post) throws ModelException;
	List<Post> listAll() throws ModelException;
	Post findById(int id) throws ModelException;

	// --- Atividade 3: paginação e busca ---
	List<Post> listPage(String search, int limit, int offset) throws ModelException;
	int count(String search) throws ModelException;
}
