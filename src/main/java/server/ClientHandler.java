package server;

import protocol.AuthRequest;
import protocol.Message;
import protocol.ServerResponse;
import protocol.MessageType;
import protocol.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerManager serverManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Зберігаємо логін клієнта, щоб знати, кого видаляти при відключенні
    private String authenticatedUsername = null;

    public ClientHandler(Socket socket, ServerManager serverManager) {
        this.socket = socket;
        this.serverManager = serverManager;
    }

    @Override
    public void run() {
        try {
            // 1. Ініціалізація потоків (порядок критично важливий!)
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // 2. Безкінечний цикл читання вхідних об'єктів
            while (true) {
                Object incomingData = in.readObject();

                // 3. Визначаємо тип об'єкта через instanceof
                if (incomingData instanceof AuthRequest) {
                    handleAuth((AuthRequest) incomingData);
                }
                else if (incomingData instanceof Message) {
                    handleMessage((Message) incomingData);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("З'єднання з клієнтом перервано.");
        } finally {
            disconnect();
        }
    }

    // Обробка запиту на вхід/реєстрацію
    private void handleAuth(AuthRequest request) {
        String username = request.getUsername();

        // TODO: Пізніше тут буде виклик AuthService для перевірки файлу з паролями.
        // Поки що ми просто погоджуємося і пускаємо всіх:

        this.authenticatedUsername = username;
        serverManager.addClient(username, this);

        // Відправляємо клієнту відповідь про успіх (і об'єкт User як payload)
        sendResponse(new ServerResponse(MessageType.AUTH_SUCCESS, new User(username)));
    }

    // Обробка звичайного повідомлення
    private void handleMessage(Message message) {
        if (authenticatedUsername != null) {
            // Віддаємо повідомлення нашому менеджеру, хай шукає адресата
            serverManager.routeMessage(message);
        }
    }

    // Метод, який викликає ServerManager, щоб відправити повідомлення ЦЬОМУ клієнту
    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush(); // Обов'язково виштовхуємо байти в мережу
        } catch (IOException e) {
            System.err.println("Помилка відправки повідомлення: " + e.getMessage());
        }
    }

    // Метод для відправки системних відповідей
    private void sendResponse(ServerResponse response) {
        try {
            out.writeObject(response);
            out.flush();
        } catch (IOException e) {
            System.err.println("Помилка відправки відповіді: " + e.getMessage());
        }
    }

    private void disconnect() {
        if (authenticatedUsername != null) {
            serverManager.removeClient(authenticatedUsername);
        }
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Помилка при закритті сокета: " + e.getMessage());
        }
    }
}