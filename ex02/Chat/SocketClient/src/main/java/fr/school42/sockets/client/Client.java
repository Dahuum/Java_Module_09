package fr.school42.sockets.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class Client {
    private static final Gson gson = new Gson(); 
    
    public static void main(String[] args) {
        int serverPort = 8081;

        for (String arg : args) {
            if (arg.startsWith("--server-port=")) {
                serverPort = Integer.parseInt(arg.substring(14));
            }
        }

        Socket socket = null;
        try {
            socket = new Socket("localhost", serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);

            Socket finalSocket = socket;
            
            // Thread to read from server and print to console
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                    // Server closed connection - exit program
                    System.exit(0);
                } catch (IOException e) {
                    // Connection closed
                }
            });
            readerThread.setDaemon(true); // Make it daemon so it doesn't block exit
            readerThread.start();

            // Main thread: read from user and send to server
            String userInput;
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                JsonObject json = new JsonObject();
                json.addProperty("message", userInput);
                
                // Send JSON instead 
                out.println(gson.toJson(json));
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}