
package fr.school42.sockets.server;

import fr.school42.sockets.repositories.MessagesRepository;
import fr.school42.sockets.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class Server 
{
	private final UserService userService;
	private final MessagesRepository messagesRepository;
	private final List<ClientHandler> connectedClients;
	
	@Autowired
	public Server(UserService userService, MessagesRepository messagesRepository ) {
		this.userService = userService;
		this.messagesRepository = messagesRepository;
		this.connectedClients = Collections.synchronizedList(new ArrayList<>());
	}

	public void start(int port) { 
		System.out.println("Starting server on  port " + port); 

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client connected: " + clientSocket.getInetAddress());

				ClientHandler ch = new ClientHandler(clientSocket, this, userService, messagesRepository);
				new Thread(ch).start(); // wa nssit threads f java, tfu, wkha ra ashal mayakun, ofc compared to c.....
			}
		} catch (IOException e) { System.err.println("Server error: " + e.getMessage()); }
	}

	public void addClient(ClientHandler client) {
		synchronized (connectedClients) {
			connectedClients.add(client);
			System.out.println("Active clients: " + connectedClients.size());
		}
	} 

	public void removeClient(ClientHandler client) {
		synchronized (connectedClients) {
			connectedClients.remove(client);
			System.out.println("Client disconnected. Active clients: " + connectedClients.size());
		}
	}

	// sender is not used, n9der nkhdem bih if i wanted to check for it, to not broadcast to it for example
	public void broadcast(String message, ClientHandler sender) { 
		synchronized (connectedClients) {
			for (ClientHandler client: connectedClients)
				client.sendMessage(message);
		}
	} 
}	
