import com.google.common.collect.Maps;
import model.PriceDataPoint;
import model.PriceHistory;
import model.UserState;
import org.junit.Test;
import org.mongodb.morphia.Morphia;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TickTests {
    @Test
    public void testSimple(){
        final Morphia morphia = new Morphia();
        Tick ticker = new Tick(morphia);
        Map<String, UserState> before = Maps.newHashMap();
        Map<String, List<PriceDataPoint>> dataPoints = Maps.newHashMap();
        PriceHistory history = new PriceHistory(dataPoints);
//        Map<String,UserState> after = ticker.calculateNewHoldings(before,history).join();

    }
}
