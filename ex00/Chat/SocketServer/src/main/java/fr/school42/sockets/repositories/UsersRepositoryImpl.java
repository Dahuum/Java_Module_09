package fr.school42.sockets.repositories;

import fr.school42.sockets.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UsersRepositoryImpl implements UsersRepository  
{
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public UsersRepositoryImpl(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		createTableIfNotExists();
	}

	private void createTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXITS users(" +
					 "id SERIAL PRIMARY KEY, " +
					 "username VARCHAR(255), UNIQUE NOT NULL, " +
					 "password VARCHAR(255), NOT NULL)";
		jdbcTemplate.execute(sql);
	}

	private static class UserRowMapper implements RowMapper<User> { 
		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new User (rs.getLong("id"), rs.getString("username"), rs.getString("password"));
		}
	}

	@Override
	public Optional<User> findById(Long id) {
		String sql = "SELECT * FROM users WHERE id = ?";
		try {
			User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
			return Optional.ofNullable(user);
		} catch (Exception e) { return Optional.empty(); }
	}

	@Override
	public Optional<User> findByUsername(String username) {
		String sql = "SELECT * FROM users WHERE username = ?";
		try {
			User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
			return Optional.ofNullable(user);
		} catch (Exception e) { return Optional.empty(); }
	}

	@Override
	public void save(User entity) {
		String sql = "INSERT INTO users (username, password) VALUE (?, ?)";
		jdbcTemplate.update(sql, entity.getUsername(), entity.getPassword());
	}

	@Override
	public void update(User entity) {
		String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
		jdbcTemplate.update(sql, entity.getUsername(), entity.getPassword(), entity.getId());
	}

	@Override
	public void delete(Long id) {
		String sql = "DELETE FROM users WHERE id = ?";
		jdbcTemplate.update(sql, id);
	}
}
