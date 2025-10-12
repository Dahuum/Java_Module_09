package fr.school42.sockets.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        int serverPort = 8081;

        for (String arg : args) {
            if (arg.startsWith("--server-port=")) {
                serverPort = Integer.parseInt(arg.substring(14));
            }
        }

        try (
            Socket socket = new Socket("localhost", serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            // Thread to read from server (receives messages)
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    // Connection closed
                }
            });
            readerThread.start();

            // Main thread: read from user and send to server
            String userInput;
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                out.println(userInput);
                
                // If user types Exit, stop
                if (userInput.equalsIgnoreCase("Exit")) {
                    break;
                }
            }

            readerThread.join(1000); // Wait for reader thread to finish

        } catch (IOException | InterruptedException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}