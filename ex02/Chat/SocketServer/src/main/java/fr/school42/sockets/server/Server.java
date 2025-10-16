package fr.school42.sockets.server;

import fr.school42.sockets.repositories.MessagesRepository;
import fr.school42.sockets.repositories.RoomsRepository;
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
public class Server {
    private final UserService userService;
    private final MessagesRepository messagesRepository;
    private final RoomsRepository roomsRepository;
    private final List<ClientHandler> connectedClients;

    @Autowired
    public Server(UserService userService, MessagesRepository messagesRepository, RoomsRepository roomsRepository) {
        this.userService = userService;
        this.messagesRepository = messagesRepository;
        this.roomsRepository = roomsRepository;
        this.connectedClients = Collections.synchronizedList(new ArrayList<>());
    }

    public void start(int port) {
        System.out.println("Starting server on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create new thread for each client
                ClientHandler clientHandler = new ClientHandler(
                    clientSocket, 
                    this, 
                    userService, 
                    messagesRepository,
                    roomsRepository
                );
                
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
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

    // Broadcast to ALL clients in a specific room
    public void broadcastToRoom(String message, Long roomId) {
        synchronized (connectedClients) {
            for (ClientHandler client : connectedClients) {
                if (client.getCurrentRoom() != null && 
                    client.getCurrentRoom().getId().equals(roomId)) {
                    client.sendMessage(message);
                }
            }
        }
    }

    // Old method for backward compatibility (ex01)
    public void broadcast(String message, ClientHandler sender) {
        synchronized (connectedClients) {
            for (ClientHandler client : connectedClients) {
                client.sendMessage(message);
            }
        }
    }
}