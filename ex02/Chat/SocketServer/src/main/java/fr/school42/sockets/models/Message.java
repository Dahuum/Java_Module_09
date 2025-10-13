package fr.school42.sockets.models;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String text;
    private Long roomId;  // NEW: Room this message belongs to
    private LocalDateTime timestamp;

    // Constructor for new messages (no id yet)
    public Message(Long senderId, String senderUsername, String text, Long roomId) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.roomId = roomId;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for messages from database (with id)
    public Message(Long id, Long senderId, String senderUsername, String text, Long roomId, LocalDateTime timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.roomId = roomId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", senderUsername='" + senderUsername + '\'' +
                ", text='" + text + '\'' +
                ", roomId=" + roomId +
                ", timestamp=" + timestamp +
                '}';
    }

    // Format for display in chat
    public String formatForChat() {
        return senderUsername + ": " + text;
    }
}
