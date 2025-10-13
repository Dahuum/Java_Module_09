package fr.school42.sockets.repositories;

import fr.school42.sockets.models.Message;
import java.util.List;

public interface MessagesRepository extends CrudRepository<Message> {
    // Get all messages (for broadcasting when someone joins)
    List<Message> findAll();
    
    // Get recent messages (useful for chat history)
    List<Message> findRecent(int limit);
    
    // NEW: Get messages by room
    List<Message> findByRoomId(Long roomId);
    
    // NEW: Get last N messages from a specific room
    List<Message> findRecentByRoomId(Long roomId, int limit);
}