package crypto.com.position;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.CharBuffer;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceTest {

    private final DataSource dataSource = new DataSource();

    @Test
    public void testLoadAndQuery() {
        dataSource.init();
        double[] params = dataSource.querySecurityDefinitions(CharBuffer.wrap("AMZN".toCharArray()));
        assertEquals(4, params.length);
        assertEquals(3380.0, params[0], 0.00000000001);
        assertEquals(2.0, params[1], 0.00000000001);
        assertEquals(2.0, params[2], 0.00000000001);
        assertEquals(1.73, params[3], 0.00000000001);
        dataSource.close();
    }
}