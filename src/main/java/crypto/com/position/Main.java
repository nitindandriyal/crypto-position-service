package crypto.com.position;

public class Main {

    public static void main(final String[] args) {
        DataSource dataSource = new DataSource();
        StockTickSubscriber stockTickSubscriber = new StockTickSubscriber();
        stockTickSubscriber.registerForTickEvents(new PositionManager(dataSource.init()));
        stockTickSubscriber.start(88, "aeron:udp?endpoint=localhost:20121", true);
        System.out.println("Shutting down...");
    }
}

