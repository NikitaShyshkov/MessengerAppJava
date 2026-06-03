package server;

import protocol.Message;
import protocol.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String URL = "jdbc:sqlite:messenger.db";

    public DatabaseService() {
        initDatabase();
    }

    private void initDatabase() {
        String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender TEXT NOT NULL, " +
                "receiver TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createMessagesTable);
            System.out.println("База даних SQLite успішно ініціалізована.");
            
        } catch (SQLException e) {
            System.err.println("Помилка БД: " + e.getMessage());
        }
    }

    public void saveMessage(Message msg) {
        String sql = "INSERT INTO messages(sender, receiver, text, timestamp) VALUES(?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, msg.getSender().getUsername());
            pstmt.setString(2, msg.getReceiver().getUsername());
            pstmt.setString(3, msg.getText());
            pstmt.setString(4, msg.getTimestamp().toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Помилка збереження повідомлення: " + e.getMessage());
        }
    }

    public List<Message> getHistory(String userA, String userB) {
        List<Message> history = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE " +
                "(sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
                "ORDER BY timestamp ASC";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userA);
            pstmt.setString(2, userB);
            pstmt.setString(3, userB);
            pstmt.setString(4, userA);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User sender = new User(rs.getString("sender"));
                User receiver = new User(rs.getString("receiver"));
                String text = rs.getString("text");
                String dbTime = rs.getString("timestamp");

                java.time.LocalDateTime ts;
                try {
                    if (dbTime.contains(" ")) {
                        dbTime = dbTime.replace(" ", "T");
                    }
                    ts = java.time.LocalDateTime.parse(dbTime);
                } catch (Exception e) {
                    System.err.println("Помилка читання часу, ставимо поточний: " + e.getMessage());
                    ts = java.time.LocalDateTime.now();
                }
                history.add(new Message(sender, receiver, text, ts));
            }
        } catch (SQLException e) {
            System.err.println("Помилка завантаження історії: " + e.getMessage());
        }
        return history;
    }
}
