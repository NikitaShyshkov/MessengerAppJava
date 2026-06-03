package client.model;

import protocol.Message;
import protocol.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatModel {
    private User currentUser;

    private final Map<User, List<Message>> chats = new HashMap<>();

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void addMessage(User contact, Message message) {
        chats.putIfAbsent(contact, new ArrayList<>());
        chats.get(contact).add(message);
    }

    public List<Message> getChatHistory(User contact) {
        return chats.getOrDefault(contact, new ArrayList<>());
    }

    public List<User> getContacts() {
        return new ArrayList<>(chats.keySet());
    }

    public void setChatHistory(User contact, List<Message> history) {
        chats.put(contact, history);
    }
}
