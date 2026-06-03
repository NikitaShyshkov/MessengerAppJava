package protocol;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MessageType type;

    private final Object payload;

    public ServerResponse(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
