
package fr.school42.sockets.server;


import fr.school42.sockets.models.Message;
import fr.school42.sockets.models.User;
import fr.school42.sockets.repositories.MessagesRepository;
import fr.school42.sockets.services.UserService;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final UserService userService;
    private final MessagesRepository messagesRepository;
    
    private PrintWriter out;
    private BufferedReader in;
    private User authenticatedUser;
    private boolean running;

    public ClientHandler(Socket socket, Server server, UserService userService, MessagesRepository messagesRepository) {
        this.socket = socket;
        this.server = server;
        this.userService = userService;
        this.messagesRepository = messagesRepository;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Hello from Server!");

            // Authentication phase
            if (!authenticate()) {
                out.println("Authentication failed. Closing connection.");
                close();
                return;
            }

            // Add to connected clients
            server.addClient(this);
            
            out.println("Start messaging");

            // Message loop
            messageLoop();

        } catch (IOException e) {
            System.err.println("Error in client handler: " + e.getMessage());
        } finally {
            close();
        }
    }

    private boolean authenticate() throws IOException {
        String command = in.readLine();
        if (command == null) return false;

        command = command.trim();

        if (command.equals("signUp")) {
            return handleSignUp();
        } else if (command.equals("signIn")) {
            return handleSignIn();
        }

        return false;
    }

    private boolean handleSignUp() throws IOException {
        out.println("Enter username:");
        String username = in.readLine();
        if (username == null || username.trim().isEmpty()) return false;

        out.println("Enter password:");
        String password = in.readLine();
        if (password == null || password.isEmpty()) return false;

        boolean success = userService.signUp(username.trim(), password);
        if (success) {
            out.println("Successful!");
            System.out.println("User registered: " + username);
            authenticatedUser = userService.getUserByUsername(username.trim());
            System.out.println("User signed in: " + username);
            // After signup, need to sign in ? 3lash alkidar
            return true; 
        } else {
            out.println("Username already exists!");
            return false;
        }
    }

    private boolean handleSignIn() throws IOException {
        out.println("Enter username:");
        String username = in.readLine();
        if (username == null || username.trim().isEmpty()) return false;

        out.println("Enter password:");
        String password = in.readLine();
        if (password == null || password.isEmpty()) return false;

        boolean success = userService.signIn(username.trim(), password);
        if (success) {
            // Get the authenticated user from database
            authenticatedUser = userService.getUserByUsername(username.trim());
            System.out.println("User signed in: " + username);
            return true;
        } else {
            out.println("Invalid credentials!");
            return false;
        }
    }

    private void messageLoop() throws IOException {
        String input;
        while (running && (input = in.readLine()) != null) {
            input = input.trim();

            if (input.equalsIgnoreCase("Exit")) {
                out.println("You have left the chat.");
                break;
            }

            if (input.isEmpty()) continue;

            // Create and save message
            Message message = new Message(
                authenticatedUser.getId(),
                authenticatedUser.getUsername(),
                input
            );
            messagesRepository.save(message);

            // Broadcast to all clients
            server.broadcast(message.formatForChat(), this);
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void close() {
        running = false;
        server.removeClient(this);
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client handler: " + e.getMessage());
        }
    }
}