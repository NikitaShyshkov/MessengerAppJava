package protocol;

import java.io.Serializable;

public class AuthRequest implements Serializable {
    // Фіксуємо версію класу для стабільної серіалізації/десеріалізації
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    // Якщо true - це запит на реєстрацію, якщо false - на звичайний вхід
    private final boolean isRegistration;

    public AuthRequest(String username, String password, boolean isRegistration) {
        this.username = username;
        this.password = password;
        this.isRegistration = isRegistration;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRegistration() {
        return isRegistration;
    }
}