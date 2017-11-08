package model.signals;

import com.google.common.collect.Lists;
import model.PriceHistory;
import model.Selector;
import model.Signal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class TrendSignal implements Signal{
    private Integer days;
    private Boolean upwardsTrend;

    public TrendSignal(Integer days, Boolean upwardsTrend) {
        this.days = days;
        this.upwardsTrend = upwardsTrend;
    }

    public TrendSignal() {
    }


    @Override
    public boolean isActive(PriceHistory priceHistory, String symbol, LocalDate date) {
        Optional<List<Double>> prices = priceHistory.getSalesPricesForSymbol(symbol,date);

        return prices.map(pri -> {
            int maxIndex = Math.min(pri.size(), days);
            List<Double> newestFirst = Lists.reverse(pri).subList(0,maxIndex);
            Double start = Double.MAX_VALUE;
            for (Double p : newestFirst) {
                if(p < start == upwardsTrend)
                    return false;
                else
                    start = p;
            }
            return false;
        })
                .orElse(false);
    }
}
