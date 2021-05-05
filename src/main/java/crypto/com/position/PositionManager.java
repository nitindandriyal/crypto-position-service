package crypto.com.position;

import crypto.com.position.events.PositionEventHandler;
import crypto.com.position.events.TickEventHandler;
import org.agrona.collections.Object2IntHashMap;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class PositionManager implements TickEventHandler {

    private final DataSource dataSource;

    private final CopyOnWriteArrayList<PositionEventHandler> positionEventHandlers = new CopyOnWriteArrayList<>();

    private final Object2IntHashMap<CharBuffer> positions = new Object2IntHashMap<>(-1);
    private final Object2IntHashMap<CharBuffer> callPositions = new Object2IntHashMap<>(-1);
    private final Object2IntHashMap<CharBuffer> putPositions = new Object2IntHashMap<>(-1);

    public PositionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PositionManager init() throws IOException {
        Scanner fileReader = new Scanner(new File("src/main/resources/positions.csv")).useDelimiter(",|\r?\n|\r");
        while (fileReader.hasNextLine()) {
            CharBuffer ticker = CharBuffer.wrap(fileReader.next().toCharArray());
            positions.put(ticker, fileReader.nextInt());

            fileReader.next();
            callPositions.put(ticker, fileReader.nextInt());

            fileReader.next();
            putPositions.put(ticker, fileReader.nextInt());
        }

        CharBuffer[] stocks = new CharBuffer[positions.size()];
        long[] stockQts = new long[positions.size()];
        long[] callQts = new long[positions.size()];
        long[] putQts = new long[positions.size()];
        int i = 0;
        for (CharBuffer stock : positions.keySet()) {
            stocks[i] = stock;
            stockQts[i] = positions.get(stock);
            callQts[i] = callPositions.get(stock);
            putQts[i] = putPositions.get(stock);
            i++;
        }

        for (i = 0; i < positionEventHandlers.size(); i++) {
            positionEventHandlers.get(i).snapshot(stocks, stockQts, callQts, putQts);
        }

        return this;
    }

    public void registerForPositionEvents(PositionEventHandler positionEventHandler) {
        positionEventHandlers.add(positionEventHandler);
    }

    private double callPrice(double s, double k, double t, double r, double d1, double d2) {
        return s * standardNormalDistribution(d1) - k * Math.exp(-r * t) * standardNormalDistribution(d2);
    }

    private double putPrice(double s, double k, double t, double r, double d1, double d2) {
        return k * Math.exp(-r * t) * standardNormalDistribution(-d2) - s * standardNormalDistribution(-d1);
    }

    private double d1(double s, double k, double t, double r, double volatility) {
        double num = Math.log(s / k) + (r + Math.pow(volatility, 2) / 2) * t;
        double den = volatility * Math.sqrt(t);
        return num / den;
    }

    private double d2(double s, double k, double t, double r, double volatility, double d1) {
        return d1 - volatility * Math.sqrt(t);
    }

    public static double standardNormalDistribution(double d) {
        double num = Math.exp(-0.5 * Math.pow(d, 2));
        double den = Math.sqrt(2 * Math.PI);
        return num / den;
    }

    @Override
    public void onEvent(CharBuffer stock, double price) {
        double[] params = dataSource.querySecurityDefinitions(stock); // k,t,r,v
        double d1 = d1(price, params[0], params[1], params[2], params[3]);
        double d2 = d2(price, params[0], params[1], params[2], params[3], d1);

        double callPrice = callPrice(price, params[0], params[1], params[2], d1, d2);
        double putPrice = putPrice(price, params[0], params[1], params[2], d1, d2);

        for (int i = 0; i < positionEventHandlers.size(); i++) {
            positionEventHandlers.get(i).update(stock, price, callPrice, putPrice);
        }
    }
}
