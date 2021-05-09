package crypto.com.position;

import crypto.com.position.events.PositionEventsPrettyPrinter;

public class Main {

    public static void main(final String[] args) throws Exception {
        DataSource dataSource = new DataSource();

        StockTickSubscriber stockTickSubscriber = new StockTickSubscriber();

        PositionEventsPrettyPrinter positionEventsPrettyPrinter = new PositionEventsPrettyPrinter();

        PositionManager positionManager = new PositionManager(dataSource.init());
        positionManager.registerForPositionEvents(positionEventsPrettyPrinter);
        positionManager.init("src/main/resources/positions.csv");

        stockTickSubscriber.registerForTickEvents(positionManager);
        stockTickSubscriber.start(88, "aeron:udp?endpoint=localhost:8888", true);

        System.out.println("Shutting down...");
        stockTickSubscriber.stop();
        dataSource.close();
    }
}

