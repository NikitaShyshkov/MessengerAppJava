package server;

import protocol.Message;
import java.util.concurrent.ConcurrentHashMap;

public class ServerManager {
    // Зберігаємо активних користувачів: Ключ (Логін) -> Значення (Потік клієнта)
    private final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    // Додаємо нового клієнта після успішної авторизації
    public void addClient(String username, ClientHandler handler) {
        activeClients.put(username, handler);
        System.out.println("Користувач підключився: " + username);
        // TODO: Пізніше ми додамо тут розсилку оновленого списку юзерів усім клієнтам
    }

    // Видаляємо клієнта, коли він відключається
    public void removeClient(String username) {
        if (username != null) {
            activeClients.remove(username);
            System.out.println("Користувач відключився: " + username);
            // TODO: Розсилка оновленого списку юзерів
        }
    }

    // Головний метод маршрутизації приватних повідомлень
    public void routeMessage(Message message) {
        String receiverUsername = message.getReceiver().getUsername();
        ClientHandler handler = activeClients.get(receiverUsername);

        if (handler != null) {
            handler.sendMessage(message);
        } else {
            System.out.println("Спроба відправити повідомлення офлайн-користувачу: " + receiverUsername);
        }
    }
}