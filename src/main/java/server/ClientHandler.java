package server;

import protocol.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerManager serverManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private String authenticatedUsername = null;

    public ClientHandler(Socket socket, ServerManager serverManager) {
        this.socket = socket;
        this.serverManager = serverManager;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object incomingData = in.readObject();

                if (incomingData instanceof AuthRequest) {
                    handleAuth((AuthRequest) incomingData);
                }
                else if (incomingData instanceof Message) {
                    handleMessage((Message) incomingData);
                }
                else if (incomingData instanceof HistoryRequest) {
                    HistoryRequest req = (HistoryRequest) incomingData;
                    List<Message> history = serverManager.getChatHistory(req.getUserA(), req.getUserB());

                    sendResponse(new ServerResponse(MessageType.HISTORY_RESPONSE, history));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("З'єднання з клієнтом перервано.");
        } finally {
            disconnect();
        }
    }

    private void handleAuth(AuthRequest request) {
        String username = request.getUsername().trim();

        if (!serverManager.addClient(username, this)) {
            sendResponse(new ServerResponse(MessageType.AUTH_ERROR, "Ім'я вже зайняте. Введіть інше ім'я."));
            return;
        }

        this.authenticatedUsername = username;

        sendResponse(new ServerResponse(MessageType.AUTH_SUCCESS, new User(username)));
        serverManager.broadcastUserList();
    }

    private void handleMessage(Message message) {
        if (authenticatedUsername != null) {
            serverManager.routeMessage(message);
        }
    }

    public synchronized void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("Помилка відправки повідомлення: " + e.getMessage());
        }
    }

    public synchronized void sendResponse(ServerResponse response) {
        try {
            out.writeObject(response);
            out.flush();
            out.reset();
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
