package crypto.com.position;

import crypto.com.position.events.TickEventHandler;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class PositionManager implements TickEventHandler {
    private final DataSource dataSource;

    private final CopyOnWriteArrayList<TickEventHandler> tickEventHandlers = new CopyOnWriteArrayList<>();

    public PositionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    void init() {

    }

    private double callPrice(double s, double k, double r, double t, double d1, double d2) {
        return s * standardNormalDistribution(d1) - k * Math.exp(-r * t) * standardNormalDistribution(d2);
    }

    private double putPrice(double s, double k, double r, double t, double d1, double d2) {
        return k * Math.exp(-r * t) * standardNormalDistribution(-d2) - s * standardNormalDistribution(-d1);
    }

    private double d1(double s, double k, double r, double t, double volatility) {
        double num = Math.log(s / k) + (r + Math.pow(volatility, 2) / 2) * t;
        double den = volatility * Math.sqrt(t);
        return num / den;
    }

    private double d2(double s, double k, double r, double t, double volatility) {
        return d1(s, k, r, t, volatility) - volatility * Math.sqrt(t);
    }

    public static double standardNormalDistribution(double d) {
        double num = Math.exp(-0.5 * Math.pow(d, 2));
        double den = Math.sqrt(2 * Math.PI);
        return num / den;
    }

    @Override
    public void onEvent(CharBuffer stock, double price) {
        double[] params = dataSource.querySecurityDefinitions(stock);
        System.out.println(Arrays.toString(params));
    }
}
