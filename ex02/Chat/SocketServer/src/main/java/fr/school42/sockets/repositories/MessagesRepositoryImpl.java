package fr.school42.sockets.repositories;

import fr.school42.sockets.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class MessagesRepositoryImpl implements MessagesRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MessagesRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS messages (" +
                     "id SERIAL PRIMARY KEY, " +
                     "sender_id BIGINT NOT NULL, " +
                     "sender_username VARCHAR(255) NOT NULL, " +
                     "text TEXT NOT NULL, " +
                     "room_id BIGINT NOT NULL, " +
                     "timestamp TIMESTAMP NOT NULL, " +
                     "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE)";
        jdbcTemplate.execute(sql);
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Message(
                rs.getLong("id"),
                rs.getLong("sender_id"),
                rs.getString("sender_username"),
                rs.getString("text"),
                rs.getLong("room_id"),
                rs.getTimestamp("timestamp").toLocalDateTime()
            );
        }
    }

    @Override
    public Optional<Message> findById(Long id) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try {
            Message message = jdbcTemplate.queryForObject(sql, new MessageRowMapper(), id);
            return Optional.ofNullable(message);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(Message entity) {
        String sql = "INSERT INTO messages (sender_id, sender_username, text, room_id, timestamp) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            entity.getSenderId(), 
            entity.getSenderUsername(), 
            entity.getText(),
            entity.getRoomId(),
            Timestamp.valueOf(entity.getTimestamp())
        );
    }

    @Override
    public void update(Message entity) {
        String sql = "UPDATE messages SET sender_id = ?, sender_username = ?, text = ?, room_id = ?, timestamp = ? WHERE id = ?";
        jdbcTemplate.update(sql, 
            entity.getSenderId(), 
            entity.getSenderUsername(), 
            entity.getText(),
            entity.getRoomId(),
            Timestamp.valueOf(entity.getTimestamp()), 
            entity.getId()
        );
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM messages WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Message> findAll() {
        String sql = "SELECT * FROM messages ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, new MessageRowMapper());
    }

    @Override
    public List<Message> findRecent(int limit) {
        String sql = "SELECT * FROM messages ORDER BY timestamp DESC LIMIT ?";
        List<Message> messages = jdbcTemplate.query(sql, new MessageRowMapper(), limit);
        java.util.Collections.reverse(messages);
        return messages;
    }

    @Override
    public List<Message> findByRoomId(Long roomId) {
        String sql = "SELECT * FROM messages WHERE room_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, new MessageRowMapper(), roomId);
    }

    @Override
    public List<Message> findRecentByRoomId(Long roomId, int limit) {
        String sql = "SELECT * FROM messages WHERE room_id = ? ORDER BY timestamp DESC LIMIT ?";
        List<Message> messages = jdbcTemplate.query(sql, new MessageRowMapper(), roomId, limit);
        java.util.Collections.reverse(messages);
        return messages;
    }
}