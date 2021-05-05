package crypto.com.position.events;

import java.nio.CharBuffer;

public interface PositionEventHandler {

    void snapshot(CharBuffer[] stocks, long[] stockQts, long[] callQts, long[] putQts);

    void update(CharBuffer stock, double price, double callPrice, double putPrice);
}
