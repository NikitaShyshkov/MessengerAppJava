package client.controller;

import client.model.ChatModel;
import client.network.NetworkClient;
import client.view.ChatView;
import protocol.Message;
import protocol.User;

import javax.swing.*;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class ClientController {
    private final ChatModel model;
    private final ChatView view;
    private final NetworkClient network;

    public ClientController(ChatModel model, ChatView view, NetworkClient network) {
        this.model = model;
        this.view = view;
        this.network = network;

        initViewListeners();
        
        this.network.setOnMessageReceived(this::handleIncomingMessage);
        this.network.setOnUserListReceived(this::handleUserListUpdate);
        this.network.setOnHistoryReceived(this::handleHistoryResponse);
    }

    private void handleUserListUpdate(List<User> onlineUsers) {
        SwingUtilities.invokeLater(() -> {
            onlineUsers.removeIf(u -> u.getUsername().equals(model.getCurrentUser().getUsername()));

            List<User> displayList = new java.util.ArrayList<>(onlineUsers);

            for (User historyContact : model.getContacts()) {
                if (!onlineUsers.contains(historyContact)) {
                    historyContact.setOnline(false);
                    displayList.add(historyContact);
                }
            }

            view.updateUserList(displayList);
        });
    }

    private void initViewListeners() {
        view.addSendButtonListener(e -> sendMessage());

        view.addUserSelectionListener(e -> {
            User selectedUser = view.getSelectedUser();
            if (!e.getValueIsAdjusting()) {
                network.requestHistory(model.getCurrentUser().getUsername(), selectedUser.getUsername());
            }
        });
    }

    private void sendMessage() {
        String text = view.getInputText().trim();
        User selectedUser = view.getSelectedUser();

        if (!text.isEmpty() && selectedUser != null) {
            Message msg = new Message(model.getCurrentUser(), selectedUser, text);
            
            network.sendMessage(msg);
            
            model.addMessage(selectedUser, msg);
            updateChatDisplay();
            
            view.clearInputField();
        } else if (selectedUser == null) {
            JOptionPane.showMessageDialog(null, "Будь ласка, оберіть співрозмовника зліва!");
        }
    }

    private void handleIncomingMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            User sender = msg.getSender();
            
            model.addMessage(sender, msg);
            
            if (sender.equals(view.getSelectedUser())) {
                updateChatDisplay();
            }
        });
    }

    private void updateChatDisplay() {
        User selected = view.getSelectedUser();
        if (selected != null) {
            List<Message> history = model.getChatHistory(selected);
            StringBuilder sb = new StringBuilder();

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            for (Message m : history) {
                String formattedTime = m.getTimestamp().format(timeFormatter);
                sb.append("[").append(formattedTime).append("] ")
                        .append(m.getSender().getUsername()).append(": ")
                        .append(m.getText()).append("\n");
            }
            
            view.updateChatArea(sb.toString());
        } else {
            view.updateChatArea("Оберіть чат для початку спілкування.");
        }
    }

    private void handleHistoryResponse(List<Message> history) {
        SwingUtilities.invokeLater(() -> {
            User selected = view.getSelectedUser();
            if (selected != null) {
                model.setChatHistory(selected, history);
                updateChatDisplay();
            }
        });
    }

    public void start() {
        view.display();
    }
}
