package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        ServerManager serverManager = new ServerManager();
        System.out.println("Сервер запущено на порту " + PORT + ". Очікування клієнтів...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Нове підключення з IP: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, serverManager);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Критична помилка сервера: " + e.getMessage());
        }
    }
}
