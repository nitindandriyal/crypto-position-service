package crypto.com.position.events;

import java.nio.CharBuffer;

public interface TickEventHandler {
    void onEvent(CharBuffer stock, double price);
}
