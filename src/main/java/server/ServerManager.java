package server;

import protocol.Message;
import protocol.MessageType;
import protocol.ServerResponse;
import protocol.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerManager {
    private final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private final DatabaseService dbService = new DatabaseService();

    public boolean addClient(String username, ClientHandler handler) {
        if (username == null || username.isBlank()) {
            return false;
        }

        if (activeClients.putIfAbsent(username, handler) != null) {
            return false;
        }

        System.out.println("Користувач підключився: " + username);
        return true;
    }

    public void removeClient(String username) {
        if (username != null) {
            activeClients.remove(username);
            System.out.println("Користувач відключився: " + username);
            broadcastUserList();
        }
    }

    public void broadcastUserList() {
        List<User> onlineUsers = new ArrayList<>();
        for (String user : activeClients.keySet()) {
            onlineUsers.add(new User(user));
        }

        ServerResponse response = new ServerResponse(MessageType.USER_LIST_UPDATE, onlineUsers);

        for (ClientHandler handler : activeClients.values()) {
            handler.sendResponse(response);
        }
    }

    public void routeMessage(Message message) {
        dbService.saveMessage(message);
        String receiverUsername = message.getReceiver().getUsername();
        ClientHandler handler = activeClients.get(receiverUsername);

        if (handler != null) {
            handler.sendMessage(message);
        } else {
            System.out.println("Спроба відправити повідомлення офлайн-користувачу: " + receiverUsername);
        }
    }

    public List<Message> getChatHistory(String userA, String userB) {
        return dbService.getHistory(userA, userB);
    }
}
