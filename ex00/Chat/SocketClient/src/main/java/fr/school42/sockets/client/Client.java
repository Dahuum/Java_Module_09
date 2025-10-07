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
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            Scanner scanner = new Scanner(System.in)
        ) {
            String serverMessage = in.readLine();
            System.out.println(serverMessage);

            System.out.print("> ");
            String command = scanner.nextLine();
            out.println(command);

            if ("signUp".equals(command)) {
                String prompt = in.readLine();
                System.out.println(prompt);
                System.out.print("> ");
                String username = scanner.nextLine();
                out.println(username);

                prompt = in.readLine();
                System.out.println(prompt);
                System.out.print("> ");
                String password = scanner.nextLine();
                out.println(password);

                String response = in.readLine();
                System.out.println(response);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
