package fr.school42.sockets.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.school42.sockets.models.Message;
import fr.school42.sockets.models.Room;
import fr.school42.sockets.models.User;
import fr.school42.sockets.repositories.MessagesRepository;
import fr.school42.sockets.repositories.RoomsRepository;
import fr.school42.sockets.services.UserService;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final UserService userService;
    private final MessagesRepository messagesRepository;
    private final RoomsRepository roomsRepository;
    private final Gson gson;
    
    private PrintWriter out;
    private BufferedReader in;
    private User authenticatedUser;
    private Room currentRoom;
    private boolean running;

    public ClientHandler(Socket socket, Server server, UserService userService, 
                        MessagesRepository messagesRepository, RoomsRepository roomsRepository) {
        this.socket = socket;
        this.server = server;
        this.userService = userService;
        this.messagesRepository = messagesRepository;
        this.roomsRepository = roomsRepository;
        this.gson = new Gson();
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

            server.addClient(this);

            // Room selection phase
            if (!selectRoom()) {
                close();
                return;
            }

            // Message loop
            messageLoop();

        } catch (IOException e) {
            System.err.println("Error in client handler: " + e.getMessage());
        } finally {
            close();
        }
    }

    private boolean authenticate() throws IOException {
        // Show menu
        out.println("1. signIn");
        out.println("2. signUp");
        out.println("3. Exit");

        String choice = in.readLine();
        if (choice == null) return false;

        choice = choice.trim();

        if      (choice.equals("1"))    return handleSignIn();
        else if (choice.equals("2"))    return handleSignUp();
        else if (choice.equals("3"))    return false;

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
            authenticatedUser = userService.getUserByUsername(username.trim());
            System.out.println("User signed in: " + username);
            return true;

        } else {
            out.println("Invalid credentials!");
            return false;
        }
    }

    private boolean selectRoom() throws IOException {
        while (true) {
            out.println("1. Create room");
            out.println("2. Choose room");
            out.println("3. Exit");

            String choice = in.readLine();
            if (choice == null) return false;

            choice = choice.trim();

            if (choice.equals("1")) {
                if (handleCreateRoom()) return true; // Room created and joined
            } else if (choice.equals("2")) {
                if (handleChooseRoom()) return true; // Room selected and joined
            } else if (choice.equals("3")) return false; // Exit
        }
    }

    private boolean handleCreateRoom() throws IOException {
        out.println("Enter room name:");
        String roomName = in.readLine();
        if (roomName == null || roomName.trim().isEmpty()) return false;
        roomName = roomName.trim();

        Room newRoom = new Room(roomName, authenticatedUser.getId());
        try {
            roomsRepository.save(newRoom);
            currentRoom = newRoom;
            roomsRepository.addUserToRoom(authenticatedUser.getId(), currentRoom.getId());
            System.out.println("7na hna");
            
            out.println(currentRoom.getName() + " created");
            System.out.println("Room created: " + roomName + " by " + authenticatedUser.getUsername());
            return true;
        } catch (Exception e) {
            out.println("Room already exists or error occurred!\nException Message: " + e.getMessage());
            return false;
        }
    }

    private boolean handleChooseRoom() throws IOException {
        List<Room> rooms = roomsRepository.findAll();

        if (rooms.isEmpty()) {
            out.println("No rooms available. Please create one.");
            return false;
        }

        out.println("Rooms:");
        for (int i = 0; i < rooms.size(); i++) {
            out.println((i + 1) + ". " + rooms.get(i).getName());
        }
        out.println((rooms.size() + 1) + ". Exit");

        String choice = in.readLine();
        if (choice == null) return false;

        try {
            int index = Integer.parseInt(choice.trim()) - 1;
            
            if (index >= 0 && index < rooms.size()) {
                currentRoom = rooms.get(index);
                roomsRepository.addUserToRoom(authenticatedUser.getId(), currentRoom.getId());
                
                out.println(currentRoom.getName() + " ---");
                
                // Show last 30 messages
                showRoomHistory();
                
                return true;
            }
            else if (index == rooms.size()) return false; // Exit option
        } catch (NumberFormatException e) {
            out.println("Invalid choice!");
        }

        return false;
    }

    private void showRoomHistory() {
        List<Message> history = messagesRepository.findRecentByRoomId(currentRoom.getId(), 30);
        for (Message msg : history) {
            out.println(msg.formatForChat());
        }
    }

    private void messageLoop() throws IOException {
        String input;
        while (running && (input = in.readLine()) != null) {
            
            // Try to parse as JSON first
            try {
                JsonObject json = gson.fromJson(input, JsonObject.class);
                
                if (json.has("message")) {
                    String messageText = json.get("message").getAsString();
                    
                    // Check if user wants to exit
                    if (messageText.equalsIgnoreCase("Exit")) {
                        out.println("You have left the chat.");
                        break;
                    }
                    
                    // SECURITY CHECKS (this is why we use JSON!)
                    
                    // Check 1: If JSON has fromId, verify it's the right user
                    if (json.has("fromId")) {
                        Long fromId = json.get("fromId").getAsLong();
                        if (!fromId.equals(authenticatedUser.getId())) {
                            out.println("ERROR: Invalid fromId! You are user " + authenticatedUser.getId());
                            continue; // Skip this message
                        }
                    }
                    
                    // Check 2: If JSON has roomId, verify it's the right room  
                    if (json.has("roomId")) {
                        Long roomId = json.get("roomId").getAsLong();
                        if (!roomId.equals(currentRoom.getId())) {
                            out.println("ERROR: Invalid roomId! You are in room " + currentRoom.getId());
                            continue; // Skip this message
                        }
                    }
                    
                    // All checks passed! Process the message normally
                    Message message = new Message(
                        authenticatedUser.getId(),        // Use the REAL user ID
                        authenticatedUser.getUsername(),  // Use the REAL username
                        messageText,                      // Use the message from JSON
                        currentRoom.getId()               // Use the REAL room ID
                    );
                    messagesRepository.save(message);
                    
                    // Broadcast normally (same as before)
                    server.broadcastToRoom(message.formatForChat(), currentRoom.getId());
                }
                
            } catch (Exception e) {
                // If not JSON, treat as plain text (for backward compatibility)
                String text = input.trim();
                
                if (text.equalsIgnoreCase("Exit")) {
                    out.println("You have left the chat.");
                    break;
                }
                
                if (text.isEmpty()) continue;
                
                // Process plain text message (same as before)
                Message message = new Message(
                    authenticatedUser.getId(),
                    authenticatedUser.getUsername(),
                    text,
                    currentRoom.getId()
                );
                messagesRepository.save(message);
                
                server.broadcastToRoom(message.formatForChat(), currentRoom.getId());
            }
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

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void close() {
        running = false;
        
        if (currentRoom != null && authenticatedUser != null) {
            roomsRepository.removeUserFromRoom(authenticatedUser.getId(), currentRoom.getId());
        }
        
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