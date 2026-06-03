package client;

import protocol.AuthRequest;
import protocol.Message;
import protocol.ServerResponse;
import protocol.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ConsoleClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8888;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть ваш логін для підключення: ");
        String myUsername = scanner.nextLine();

        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Підключено до сервера!");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            AuthRequest authRequest = new AuthRequest(myUsername, "dummy_password", false);
            out.writeObject(authRequest);
            out.flush();

            User me = new User(myUsername);

            Thread readerThread = new Thread(() -> {
                try {
                    while (true) {
                        Object incoming = in.readObject();
                        if (incoming instanceof ServerResponse) {
                            ServerResponse response = (ServerResponse) incoming;
                            System.out.println("[СЕРВЕР]: " + response.getType());
                        } else if (incoming instanceof Message) {
                            Message msg = (Message) incoming;
                            System.out.println("\n[" + msg.getSender().getUsername() + "]: " + msg.getText());
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("З'єднання втрачено.");
                }
            });
            readerThread.start();

            System.out.println("✍Можете писати повідомлення. Формат: [Кому] [Текст]");
            System.out.println("Приклад: Ivan Привіт, як справи?");

            while (true) {
                String input = scanner.nextLine();

                String[] parts = input.split(" ", 2);
                if (parts.length == 2) {
                    User receiver = new User(parts[0]);
                    String text = parts[1];

                    Message msg = new Message(me, receiver, text);
                    out.writeObject(msg);
                    out.flush();
                } else {
                    System.out.println("Неправильний формат. Використовуйте: ІМ'Я ТЕКСТ");
                }
            }

        } catch (IOException e) {
            System.err.println("Помилка підключення: " + e.getMessage());
        }
    }
}
