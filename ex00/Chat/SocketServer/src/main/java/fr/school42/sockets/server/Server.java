package fr.school42.sockets.server;

import java.net.*;
import java.io.*;

public class Server {
    public static void main( String[] args ) throws IOException 
	{
    	int port = 8081;
		System.out.println("Starting server on  port " + port); 

		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket = serverSocket.accept();

		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		out.println("Hello from Server!!");

		// Wait for client to type SignUP
		String input = in.readLine();
		System.out.println("Client said: " + input);

		serverSocket.close();
		clientSocket.close();
	}
}
