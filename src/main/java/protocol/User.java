package protocol;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    // Рекомендується додавати serialVersionUID для стабільної серіалізації
    private static final long serialVersionUID = 1L;

    private String username;
    private boolean isOnline;

    public User(String username) {
        this.username = username;
        this.isOnline = false;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username + (isOnline ? " (Online)" : " (Offline)");
    }
}