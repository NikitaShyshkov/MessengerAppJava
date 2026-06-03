package client.network;

import protocol.AuthRequest;
import protocol.Message;
import protocol.MessageType;
import protocol.ServerResponse;
import protocol.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NetworkClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean readingStarted;

    private Consumer<Message> onMessageReceived;
    private Consumer<java.util.List<Message>> onHistoryReceived;
    private Consumer<java.util.List<protocol.User>> onUserListReceived;

    public boolean connect(String serverAddress, int port, String username) throws IOException {
        socket = new Socket(serverAddress, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        AuthRequest authRequest = new AuthRequest(username, "dummy_password", false);
        out.writeObject(authRequest);
        out.flush();

        try {
            Object incomingData = in.readObject();
            if (!(incomingData instanceof ServerResponse)) {
                close();
                throw new IOException("Сервер повернув некоректну відповідь авторизації.");
            }

            ServerResponse response = (ServerResponse) incomingData;
            if (response.getType() == MessageType.AUTH_SUCCESS) {
                return true;
            }

            if (response.getType() == MessageType.AUTH_ERROR) {
                close();
                return false;
            }

            close();
            throw new IOException("Сервер повернув неочікувану відповідь авторизації: " + response.getType());
        } catch (ClassNotFoundException e) {
            close();
            throw new IOException("Не вдалося прочитати відповідь авторизації.", e);
        }
    }

    public void setOnMessageReceived(Consumer<Message> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void setOnUserListReceived(Consumer<java.util.List<protocol.User>> onUserListReceived) {
        this.onUserListReceived = onUserListReceived;
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Помилка відправки повідомлення: " + e.getMessage());
        }
    }

    public void setOnHistoryReceived(Consumer<java.util.List<Message>> onHistoryReceived) {
        this.onHistoryReceived = onHistoryReceived;
    }

    public void requestHistory(String myUsername, String contactUsername) {
        try {
            out.writeObject(new protocol.HistoryRequest(myUsername, contactUsername));
            out.flush();
        } catch (IOException e) {
            System.err.println("Помилка запиту історії: " + e.getMessage());
        }
    }

    public void startListening() {
        if (!readingStarted) {
            readingStarted = true;
            startReadingThread();
        }
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Помилка закриття з'єднання: " + e.getMessage());
        }
    }

    private void startReadingThread() {
        Thread readerThread = new Thread(() -> {
            try {
                while (true) {
                    Object incomingData = in.readObject();
                    
                    if (incomingData instanceof Message) {
                        if (onMessageReceived != null) {
                            onMessageReceived.accept((Message) incomingData);
                        }
                    } else if (incomingData instanceof ServerResponse) {
                        ServerResponse response = (ServerResponse) incomingData;
                        System.out.println("🔧 [СЕРВЕР]: " + response.getType());
                        if (response.getType() == protocol.MessageType.USER_LIST_UPDATE) {
                            if (onUserListReceived != null) {
                                List<protocol.User> onlineUsers = toUserList(response.getPayload());
                                onUserListReceived.accept(onlineUsers);
                            }
                        } else if (response.getType() == protocol.MessageType.HISTORY_RESPONSE) {
                            if (onHistoryReceived != null) {
                                List<Message> history = toMessageList(response.getPayload());
                                onHistoryReceived.accept(history);
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("⚠️ З'єднання з сервером перервано.");
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private List<User> toUserList(Object payload) {
        List<User> users = new ArrayList<>();
        if (payload instanceof List<?>) {
            for (Object item : (List<?>) payload) {
                if (item instanceof User) {
                    users.add((User) item);
                }
            }
        }
        return users;
    }

    private List<Message> toMessageList(Object payload) {
        List<Message> messages = new ArrayList<>();
        if (payload instanceof List<?>) {
            for (Object item : (List<?>) payload) {
                if (item instanceof Message) {
                    messages.add((Message) item);
                }
            }
        }
        return messages;
    }
}
