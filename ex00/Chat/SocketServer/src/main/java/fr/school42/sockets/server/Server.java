package fr.school42.sockets.server;

import java.net.*;
import java.io.*;


import fr.school42.sockets.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Server 
{
	private final UserService userService;
	
	@Autowired
	public Server(UserService userService) {
		this.userService = userService;
	}

	public void start(int port) { 
		System.out.println("Starting server on  port " + port); 

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client connected: " + clientSocket.getInetAddress());

				handleClient(clientSocket);
			}
		} catch (IOException e) { System.err.println("Server error: " + e.getMessage()); }
	}

	private void handleClient(Socket clientSocket) {
		try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) 
		{
			out.println("Hello from Server!");

			String input = in.readLine();
			System.out.println("Client said: " + input);

			if (input.toLowerCase().equals("signup")) {
				out.println("Enter username:");
				String username = in.readLine();
				out.println("Enter password:");
				String password = in.readLine();

				boolean success = userService.signUp(username, password);
				if (success) {
					out.println("Successful!");
					System.out.println("User registred: " + username);
				} else {
					out.println("User already exits!");
					System.err.println("Registration failed for: " + username);
				}
			}
		} 
		catch (IOException e) { System.err.println("Error handling client: " + e.getMessage()); }
		finally {
			try {
				clientSocket.close();
			} catch (IOException e) { System.err.println("Error closing client socket: " + e.getMessage()); }
		}
	}
}
