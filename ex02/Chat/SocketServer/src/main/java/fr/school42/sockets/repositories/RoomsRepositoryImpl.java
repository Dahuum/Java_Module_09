package fr.school42.sockets.repositories;

import fr.school42.sockets.models.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class RoomsRepositoryImpl implements RoomsRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RoomsRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() {
        // Create rooms table
        String roomsSql = "CREATE TABLE IF NOT EXISTS rooms (" +
                         "id SERIAL PRIMARY KEY, " +
                         "name VARCHAR(255) UNIQUE NOT NULL, " +
                         "owner_id BIGINT NOT NULL, " +
                         "FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE)";
        jdbcTemplate.execute(roomsSql);

        // Create junction table for users in rooms
        String roomUsersSql = "CREATE TABLE IF NOT EXISTS room_users (" +
                             "room_id BIGINT NOT NULL, " +
                             "user_id BIGINT NOT NULL, " +
                             "PRIMARY KEY (room_id, user_id), " +
                             "FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE, " +
                             "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
        jdbcTemplate.execute(roomUsersSql);
    }

    private static class RoomRowMapper implements RowMapper<Room> {
        @Override
        public Room mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Room(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("owner_id")
            );
        }
    }

    @Override
    public Optional<Room> findById(Long id) {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try {
            Room room = jdbcTemplate.queryForObject(sql, new RoomRowMapper(), id);
            return Optional.ofNullable(room);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(Room entity) {
        String sql = "INSERT INTO rooms (name, owner_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getName());
            ps.setLong(2, entity.getOwnerId());
            return ps;
        }, keyHolder);
        
        if (keyHolder.getKey() != null) {
            entity.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public void update(Room entity) {
        String sql = "UPDATE rooms SET name = ?, owner_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, entity.getName(), entity.getOwnerId(), entity.getId());
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Room> findAll() {
        String sql = "SELECT * FROM rooms ORDER BY id ASC";
        return jdbcTemplate.query(sql, new RoomRowMapper());
    }

    @Override
    public void addUserToRoom(Long userId, Long roomId) {
        String sql = "INSERT INTO room_users (room_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, roomId, userId);
    }

    @Override
    public void removeUserFromRoom(Long userId, Long roomId) {
        String sql = "DELETE FROM room_users WHERE room_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, roomId, userId);
    }

    @Override
    public boolean isUserInRoom(Long userId, Long roomId) {
        String sql = "SELECT COUNT(*) FROM room_users WHERE room_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomId, userId);
        return count != null && count > 0;
    }
}