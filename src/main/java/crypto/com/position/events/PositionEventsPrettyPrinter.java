package crypto.com.position.events;

import org.agrona.collections.Object2IntHashMap;

import java.nio.CharBuffer;

public class PositionEventsPrettyPrinter implements PositionEventHandler {

    private Object2IntHashMap<CharBuffer> tickerToIndex;

    private CharBuffer[] stocks;
    private long[] stockQts;
    private long[] callQts;
    private long[] putQts;

    private double[] stockPrices;
    private double[] callPrices;
    private double[] putPrices;

    private int sequence = 1;

    @Override
    public void snapshot(CharBuffer[] stocks, long[] stockQts, long[] callQts, long[] putQts) {
        tickerToIndex = new Object2IntHashMap<>(stocks.length);
        for (int i = 0; i < stocks.length; i++) {
            tickerToIndex.put(stocks[i], i);
        }
        this.stocks = stocks;
        this.stockQts = stockQts;
        this.callQts = callQts;
        this.putQts = putQts;

        stockPrices = new double[stocks.length];
        callPrices = new double[stocks.length];
        putPrices = new double[stocks.length];
    }

    @Override
    public void update(CharBuffer stock, double price, double callPrice, double putPrice) {
        int index = tickerToIndex.get(stock);
        stockPrices[index] = price;
        callPrices[index] = price;
        putPrices[index] = price;

        System.out.printf("## %d Market Data Update%n", sequence++);
        System.out.printf("%s change to %f", stock, price);
        System.out.println();
        System.out.println();
        System.out.println("## Portfolio");
        System.out.printf("%-15s %20s %20s %23s", "symbol", "price", "qty", "value");
        System.out.println();
        double totalPortfolio = 0.0;
        for (int i = 0; i < stocks.length; i++) {
            double value = stockPrices[i] * stockQts[i];
            totalPortfolio += value;
            System.out.printf("%-15s %20f %20d %23f", stocks[i], stockPrices[i], stockQts[i], value);
            System.out.println();

            value = callPrices[i] * callQts[i];
            totalPortfolio += value;
            System.out.printf("%-15s %20f %20d %23f", stocks[i] + "-C", callPrices[i], callQts[i], value);
            System.out.println();

            value = putPrices[i] * putQts[i];
            totalPortfolio += value;
            System.out.printf("%-15s %20f %20d %23f", stocks[i] + "-P", putPrices[i], putQts[i], value);
            System.out.println();
        }
        System.out.println();
        System.out.printf("#Total Portfolio %64f", totalPortfolio);
        System.out.println();
        System.out.println();
    }
}
