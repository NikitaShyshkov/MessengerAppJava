package client.view;

import protocol.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import java.util.List;

public class ChatView {
    private JFrame frame;
    private JList<User> userList;
    private DefaultListModel<User> listModel;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public ChatView() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Месенджер");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane leftScroll = new JScrollPane(userList);
        leftScroll.setPreferredSize(new Dimension(200, 0));

        JPanel rightPanel = new JPanel(new BorderLayout());
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Відправити");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        rightPanel.add(chatScroll, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightPanel);
        splitPane.setDividerLocation(200);

        frame.add(splitPane);
    }

    public void display() {
        frame.setVisible(true);
    }

    public void updateUserList(List<User> users) {
        listModel.clear();
        for (User u : users) {
            listModel.addElement(u);
        }
    }

    public void updateChatArea(String text) {
        chatArea.setText(text);
    }

    public void clearInputField() {
        inputField.setText("");
    }

    public String getInputText() {
        return inputField.getText();
    }

    public User getSelectedUser() {
        return userList.getSelectedValue();
    }

    public void addSendButtonListener(ActionListener listener) {
        sendButton.addActionListener(listener);
        inputField.addActionListener(listener);
    }

    public void addUserSelectionListener(ListSelectionListener listener) {
        userList.addListSelectionListener(listener);
    }
}
