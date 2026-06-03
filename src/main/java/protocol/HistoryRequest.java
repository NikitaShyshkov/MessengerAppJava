package protocol;

import java.io.Serializable;

public class HistoryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userA;
    private final String userB;

    public HistoryRequest(String userA, String userB) {
        this.userA = userA;
        this.userB = userB;
    }

    public String getUserA() { return userA; }
    public String getUserB() { return userB; }
}