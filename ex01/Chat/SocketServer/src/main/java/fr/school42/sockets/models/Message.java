package fr.school42.sockets.models;

import java.time.LocalDateTime;

public class Message {
	private Long id;
	private Long senderId;
	private String senderUsername;
	private String text;
	private LocalDateTime timestamp;
	
	// this for 
	public Message(Long senderId, String senderUsername, String text) {
		this.senderId = senderId;
		this.senderUsername = senderUsername;
		this.text = text;
		this.timestamp = LocalDateTime.now();
	}
	// hada dial db
	public Message(Long id, Long senderId, String senderUsername, String text, LocalDateTime timestamp) {
		this.id = id;	
		this.senderId = senderId;
		this.senderUsername = senderUsername;
		this.text = text;
		this.timestamp = timestamp;
	}
	
	// Getters - Setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public Long getSenderId() { return senderId; }
	public void setSenderId(Long senderId) { this.senderId = senderId; }

	public String getSenderUsername() { return senderUsername; }
	public void setSenderUsername(String username) { this.senderUsername = username; } 

	public String getText() { return text; }
	public void setText() { this.text = text; }

	public LocalDateTime getTimestamp() { return timestamp; }
	public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

	@Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", senderUsername='" + senderUsername + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public String formatForChat() {
        return senderUsername + ": " + text;
    }
}
