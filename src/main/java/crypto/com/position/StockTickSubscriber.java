package crypto.com.position;

import crypto.com.position.events.TickEventHandler;
import io.aeron.Aeron;
import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.CloseHelper;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.SigInt;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class StockTickSubscriber {

    private final CopyOnWriteArrayList<TickEventHandler> tickEventHandlers = new CopyOnWriteArrayList<>();
    private MediaDriver driver;

    public void start(int streamId, String channel, boolean isEmbedded) {
        System.out.println("Subscribing to " + channel + " on stream id " + streamId);

        driver = isEmbedded ? MediaDriver.launchEmbedded() : null;
        final Aeron.Context ctx = new Aeron.Context();

        if (isEmbedded) {
            ctx.aeronDirectoryName(driver.aeronDirectoryName());
        }
        final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        final FragmentHandler fragmentHandler = (buffer, offset, length, header) -> {
            byte[] stockName = new byte[4];
            try {
                buffer.getBytes(offset, stockName);
                CharBuffer stockNameBuffer = decoder.decode(ByteBuffer.wrap(stockName));
                double price = buffer.getDouble(offset + 4);
                for (int i = 0; i < tickEventHandlers.size(); i++) {
                    tickEventHandlers.get(i).onEvent(stockNameBuffer, price);
                }
            } catch (CharacterCodingException e) {
                throw new IllegalArgumentException("Could not decode the stock name");
            }
        };

        final AtomicBoolean running = new AtomicBoolean(true);

        SigInt.register(() -> running.set(false));

        try (Aeron aeron = Aeron.connect(ctx);
             Subscription subscription = aeron.addSubscription(channel, streamId)) {
            final FragmentAssembler assembler = new FragmentAssembler(fragmentHandler);
            while (running.get()) {
                BusySpinIdleStrategy.INSTANCE.idle(subscription.poll(assembler, 10));
            }
        }
    }

    public void registerForTickEvents(TickEventHandler tickEventHandler) {
        tickEventHandlers.add(tickEventHandler);
    }

    public void stop() {
        CloseHelper.close(driver);
    }

}
