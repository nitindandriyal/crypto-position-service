package crypto.com.position;


import crypto.com.position.events.PositionEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PositionManagerTest {
    private PositionManager positionManager;

    @Mock
    private DataSource dataSource;

    @Mock
    private PositionEventHandler positionEventHandler;

    @Captor
    private ArgumentCaptor<CharBuffer[]> stocks = ArgumentCaptor.forClass(CharBuffer[].class);

    @Captor
    private ArgumentCaptor<long[]> stockQts = ArgumentCaptor.forClass(long[].class);

    @Captor
    private ArgumentCaptor<long[]> callQts = ArgumentCaptor.forClass(long[].class);

    @Captor
    private ArgumentCaptor<long[]> putQts = ArgumentCaptor.forClass(long[].class);

    @Captor
    private ArgumentCaptor<CharBuffer> stock = ArgumentCaptor.forClass(CharBuffer.class);

    @Captor
    private ArgumentCaptor<Double> price = ArgumentCaptor.forClass(Double.class);

    @Captor
    private ArgumentCaptor<Double> callPrice = ArgumentCaptor.forClass(Double.class);

    @Captor
    private ArgumentCaptor<Double> putPrice = ArgumentCaptor.forClass(Double.class);

    @Before
    public void setup() {
        positionManager = new PositionManager(dataSource);
        positionManager.registerForPositionEvents(positionEventHandler);
    }

    @Test
    public void testInitLoad() throws IOException {
        positionManager.init("src/test/resources/test-positions1.csv");
        verify(positionEventHandler, times(1)).snapshot(stocks.capture(), stockQts.capture(), callQts.capture(), putQts.capture());
        assertEquals(5, stocks.getValue().length);
        assertEquals(5, stockQts.getValue().length);
        assertEquals(5, callQts.getValue().length);
        assertEquals(5, putQts.getValue().length);
        Set<CharBuffer> expectedStocks = new HashSet<>();
        expectedStocks.add(CharBuffer.wrap("AMZN".toCharArray()));
        expectedStocks.add(CharBuffer.wrap("GOOG".toCharArray()));
        expectedStocks.add(CharBuffer.wrap("APPL".toCharArray()));
        expectedStocks.add(CharBuffer.wrap("MCST".toCharArray()));
        expectedStocks.add(CharBuffer.wrap("TSLA".toCharArray()));
        for (int i = 0; i < expectedStocks.size(); i++) {
            assertTrue(expectedStocks.contains(stocks.getValue()[i]));
        }
    }

    @Test
    public void testPriceUpdate() throws IOException {
        positionManager.init("src/test/resources/test-positions1.csv");
        CharBuffer amzn = CharBuffer.wrap("AMZN".toCharArray());
        when(dataSource.querySecurityDefinitions(amzn)).thenReturn(new double[]{
                3380.0, 2, 2.0, 1.73
        });
        positionManager.onEvent(amzn, 3386.18);
        verify(positionEventHandler, times(1)).update(stock.capture(), price.capture(), callPrice.capture(), putPrice.capture());
        assertEquals(CharBuffer.wrap("AMZN".toCharArray()), stock.getValue());
        assertEquals(3386.18, price.getValue(), 0.000000001);
        assertEquals(7.105427357601002E-15, callPrice.getValue(), 0.000000001);
        assertEquals(-7.105427357601002E-15, putPrice.getValue(), 0.000000001);
        dataSource.close();
    }


}