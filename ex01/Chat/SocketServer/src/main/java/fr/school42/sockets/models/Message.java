package fr.school42.sockets.models;

import java.time.LocalDateTime;

public class Message {
	private Long id;
	private Long senderId;
	private String senderUsername;
	private String text;
	private LocalDateTime timestamp;
	
	// this for 
	public Messgae(Long senderId, String senderUsername, String text) {
		this.senderId = senderId;
		this.senderUsername = senderUsername;
		this.text = text;
	}
	// hada dial db
	public Messgae(Long id, Long senderId, String senderUsername, String text) {
		this.id = id;	
		this.senderId = senderId;
		this.senderUsername = senderUsername;
		this.text = text;
	}
}
