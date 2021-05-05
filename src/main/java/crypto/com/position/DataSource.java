package crypto.com.position;

import org.agrona.collections.Object2ObjectHashMap;

import java.nio.CharBuffer;
import java.sql.*;
import java.util.Map;

public class DataSource {

    private final Map<CharBuffer, double[]> securityDefinitions = new Object2ObjectHashMap<>();
    private Connection connection;

    public DataSource init() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("drop table if exists security_definitions");
            statement.executeUpdate("create table security_definitions (stock string, strike_price double, maturity_years double, interest_rate double, volatility double)");
            statement.executeUpdate("insert into security_definitions values('TSLA', 605.0, 2, 2.0, 1.73)");
            statement.executeUpdate("insert into security_definitions values('APPL', 129.0, 2, 2.0, 1.73)");
            statement.executeUpdate("insert into security_definitions values('MCST', 248.0, 2, 2.0, 1.73)");
            statement.executeUpdate("insert into security_definitions values('AMZN', 3380.0, 2, 2.0, 1.73)");
            statement.executeUpdate("insert into security_definitions values('GOOG', 2339.0, 2, 2.0, 1.73)");
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        return this;
    }

    public double[] querySecurityDefinitions(CharBuffer stockName) {
        if (securityDefinitions.containsKey(stockName)) {
            return securityDefinitions.get(stockName);
        }
        try {
            PreparedStatement statement = connection.prepareStatement("select strike_price, maturity_years, interest_rate, volatility from security_definitions where stock = ?");
            statement.setString(1, stockName.toString());
            ResultSet rs = statement.executeQuery();
            double[] values = new double[4];
            while (rs.next()) {
                values[0] = rs.getDouble("strike_price"); //k
                values[1] = rs.getDouble("maturity_years"); //t
                values[2] = rs.getDouble("interest_rate"); //r
                values[3] = rs.getDouble("volatility"); //v
            }
            securityDefinitions.put(stockName, values);
            return values;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public void close() {
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException sqlException) {
                // eat
            }
        }
    }

}
