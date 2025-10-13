package fr.school42.sockets.repositories;

import fr.school42.sockets.models.Room;
import java.util.List;

public interface RoomsRepository extends CrudRepository<Room> {
    List<Room> findAll();
    void addUserToRoom(Long userId, Long roomId);
    void removeUserFromRoom(Long userId, Long roomId);
    boolean isUserInRoom(Long userId, Long roomId);
}