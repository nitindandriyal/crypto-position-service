package crypto.com.position;


import crypto.com.position.events.TickEventHandler;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.driver.MediaDriver;
import org.agrona.CloseHelper;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StockTickSubscriberTest {
    private final StockTickSubscriber stockTickSubscriber = new StockTickSubscriber();

    @Mock
    private TickEventHandler tickEventHandler;

    @Captor
    ArgumentCaptor<CharBuffer> stockCaptor = ArgumentCaptor.forClass(CharBuffer.class);

    @Captor
    private ArgumentCaptor<Double> priceCaptor = ArgumentCaptor.forClass(Double.class);

    @Test
    public void testPubSubs() throws InterruptedException {
        stockTickSubscriber.registerForTickEvents(tickEventHandler);
        new Thread(() -> stockTickSubscriber.start(8899, "aeron:udp?endpoint=localhost:8899", true)).start();

        final MediaDriver driver = MediaDriver.launchEmbedded();
        final Aeron.Context ctx = new Aeron.Context();
        ctx.aeronDirectoryName(driver.aeronDirectoryName());
        byte[] goog = "GOOG".getBytes(StandardCharsets.UTF_8);
        try (Aeron aeron = Aeron.connect(ctx); Publication publication = aeron.addPublication("aeron:udp?endpoint=localhost:8899", 8899)) {
            Thread.sleep(200);
            final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocate(256));
            buffer.putBytes(0, goog);
            buffer.putDouble(goog.length, 2344.68);
            if (publication.isConnected()) {
                publication.offer(buffer);
            } else {
                Assert.fail();
            }
        }

        verify(tickEventHandler, times(1)).onEvent(stockCaptor.capture(), priceCaptor.capture());

        CloseHelper.close(driver);

        stockTickSubscriber.stop();
    }


}
