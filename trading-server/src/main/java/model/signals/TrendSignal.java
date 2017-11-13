package model.signals;

import com.google.common.collect.Lists;
import model.PriceHistory;
import model.Selector;
import model.Signal;
import org.apache.commons.collections4.ListUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
        List<Double> prices = priceHistory.getSalesPriceLastXDays(symbol,date, days +1);
        //assume: sorted by date in descending order
        if(prices.isEmpty()){
            return false;
        } else {
            Double current = prices.get(0);
            Stream<Double> rest = prices.subList(1,prices.size()).stream();
            //if the subsequent numbers are all greater than the next we have an upwards trend
            return rest.reduce(current, this::followsTrend) > 0;
        }
    }

    private Double followsTrend(Double acc, Double next){
        if(acc < 0)
            return acc;
        else
            return Objects.equals(acc, next) || ((next > acc) == upwardsTrend) ? next : -1;
    }
}
