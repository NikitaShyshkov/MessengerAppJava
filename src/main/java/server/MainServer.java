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
                // Метод accept() зупиняє виконання коду і чекає, поки не підключиться клієнт
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 Нове підключення з IP: " + clientSocket.getInetAddress());

                // Створюємо окремий обробник для цього підключення і запускаємо його в новому потоці
                ClientHandler clientHandler = new ClientHandler(clientSocket, serverManager);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Критична помилка сервера: " + e.getMessage());
        }
    }
}