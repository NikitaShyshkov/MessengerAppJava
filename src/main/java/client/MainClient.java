package client;

import client.controller.ClientController;
import client.model.ChatModel;
import client.network.NetworkClient;
import client.view.ChatView;
import protocol.User;

import javax.swing.*;

public class MainClient {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String username;
        NetworkClient network;

        while (true) {
            username = JOptionPane.showInputDialog(
                    null,
                    "Введіть ваш логін:",
                    "Вхід у Месенджер",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (username == null || username.trim().isEmpty()) {
                System.exit(0);
            }

            username = username.trim();
            network = new NetworkClient();

            try {
                if (network.connect("localhost", 8888, username)) {
                    break;
                }
                JOptionPane.showMessageDialog(null, "Це ім'я вже зайняте. Введіть інше ім'я.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Не вдалося підключитися до сервера!");
                System.exit(1);
            }
        }

        ChatModel model = new ChatModel();
        model.setCurrentUser(new User(username));

        ChatView view = new ChatView();

        ClientController controller = new ClientController(model, view, network);
        network.startListening();

        SwingUtilities.invokeLater(() -> controller.start());
    }
}
