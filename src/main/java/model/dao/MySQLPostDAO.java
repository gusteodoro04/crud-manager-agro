package model.dao;

import java.util.ArrayList;
import java.util.List;

import model.ModelException;
import model.Post;
import model.User;

public class MySQLPostDAO implements PostDAO {

	// SELECT que já traz os dados do autor (um JOIN só, em vez de uma consulta por post)
	private static final String SELECT = "SELECT p.id, p.content, p.post_date, p.user_id, "
			+ " u.nome, u.sexo, u.email "
			+ " FROM posts p "
			+ " INNER JOIN users u ON u.id = p.user_id ";

	@Override
	public boolean save(Post post) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlInsert = "INSERT INTO posts VALUES "
				+ " (DEFAULT, ?, CURDATE(), ?);";

		db.prepareStatement(sqlInsert);
		db.setString(1, post.getContent());
		db.setInt(2, post.getUser().getId());

		return db.executeUpdate() > 0;
	}

	@Override
	public boolean update(Post post) throws ModelException {

		DBHandler db = new DBHandler();

		String sqlUpdate = "UPDATE posts "
						 + " SET content = ?,"
						 + " user_id = ?"
						 + " WHERE id = ?";

		db.prepareStatement(sqlUpdate);

		db.setString(1, post.getContent());
		db.setInt(2, post.getUser().getId());
		db.setInt(3, post.getId());

		return db.executeUpdate() > 0;
	}

	@Override
	public boolean delete(Post post) throws ModelException {
		DBHandler db = new DBHandler();

		String sqlDelete = " DELETE FROM posts "
		         + " WHERE id = ?;";

		db.prepareStatement(sqlDelete);
		db.setInt(1, post.getId());

		return db.executeUpdate() > 0;
	}

	@Override
	public List<Post> listAll() throws ModelException {

		DBHandler db = new DBHandler();

		List<Post> posts = new ArrayList<Post>();

		// Declara uma instrução SQL
		String sqlQuery = SELECT + " ORDER BY p.post_date DESC, p.id DESC";

		db.createStatement();

		db.executeQuery(sqlQuery);

		while (db.next()) {
			Post p = createPost(db);

			posts.add(p);
		}

		return posts;
	}

	@Override
	public List<Post> listPage(String search, int limit, int offset) throws ModelException {

		DBHandler db = new DBHandler();

		List<Post> posts = new ArrayList<Post>();

		String sqlQuery = SELECT
				+ " WHERE p.content LIKE ? OR u.nome LIKE ? "
				+ " ORDER BY p.post_date DESC, p.id DESC "
				+ " LIMIT ? OFFSET ?;";

		String pattern = DBHandler.like(search);

		db.prepareStatement(sqlQuery);
		db.setString(1, pattern);
		db.setString(2, pattern);
		db.setInt(3, limit);
		db.setInt(4, offset);
		db.executeQuery();

		while (db.next()) {
			posts.add(createPost(db));
		}

		return posts;
	}

	@Override
	public int count(String search) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = "SELECT COUNT(*) AS total FROM posts p "
				+ " INNER JOIN users u ON u.id = p.user_id "
				+ " WHERE p.content LIKE ? OR u.nome LIKE ?;";
		String pattern = DBHandler.like(search);

		try {
			db.prepareStatement(sql);
			db.setString(1, pattern);
			db.setString(2, pattern);
			db.executeQuery();

			return db.next() ? db.getInt("total") : 0;
		} finally {
			db.close();
		}
	}

	@Override
	public Post findById(int id) throws ModelException {

		DBHandler db = new DBHandler();

		String sql = SELECT + " WHERE p.id = ?;";

		try {
			db.prepareStatement(sql);
			db.setInt(1, id);
			db.executeQuery();

			return db.next() ? createPost(db) : null;
		} finally {
			db.close();
		}
	}

	private Post createPost(DBHandler db) throws ModelException {
		Post p = new Post(db.getInt("id"));
		p.setContent(db.getString("content"));
		p.setPostDate(db.getDate("post_date"));

		User user = new User(db.getInt("user_id"));
		user.setName(db.getString("nome"));
		user.setGender(db.getString("sexo"));
		user.setEmail(db.getString("email"));
		p.setUser(user);

		return p;
	}
}
