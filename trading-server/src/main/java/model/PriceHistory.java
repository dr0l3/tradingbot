package model;


import com.google.common.collect.Lists;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PriceHistory {
    private Map<String,List<PriceDataPoint>> dataPoints;

    public PriceHistory(Map<String, List<PriceDataPoint>> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public Optional<List<Double>> getSalesPricesForSymbol(String symbol, LocalDate date){
        List<Double> prices = dataPoints.getOrDefault(symbol, Lists.newArrayList()).stream()
                .filter(pdp -> pdp.getDate().isBefore(date.plusDays(1)))
                .map(PriceDataPoint::getAdjClose)
                .collect(Collectors.toList());
        return prices.isEmpty()? Optional.empty() : Optional.of(prices);
    }

    public List<Double> getSalesPriceLastXDays(String symbol, LocalDate date, int days){
        return dataPoints.getOrDefault(symbol, Lists.newArrayList()).stream()
                .filter(Objects::nonNull)
                .filter(pdp -> pdp.getDate() != null)
                .filter(pdp-> pdp.getAdjClose() != null)
                .filter(pdp -> pdp.getDate().isBefore(date.plusDays(1)))
                .sorted(Comparator.comparing(PriceDataPoint::getDate).reversed())
                .limit(days+1)
                .map(PriceDataPoint::getAdjClose)
                .collect(Collectors.toList());
    }

    public Optional<Double> getPriceForSymbol(String symbol, LocalDate currentDate){
        return dataPoints.getOrDefault(symbol, Lists.newArrayList()).stream()
                .filter(Objects::nonNull)
                .filter(pdp -> pdp.getDate() != null)
                .filter(pdp-> pdp.getAdjClose() != null)
                .filter(pdp -> pdp.getDate().isBefore(currentDate.plusDays(1)))
                .sorted(Comparator.comparing(PriceDataPoint::getDate).reversed())
                .findFirst()
                .map(PriceDataPoint::getAdjClose);
    }

    public Optional<Double> getSalesPriceForSymbolYesterday(String symbol, LocalDate currentDate){
        return dataPoints.getOrDefault(symbol, Lists.newArrayList()).stream()
                .filter(pdp -> pdp.getDate().isBefore(currentDate))
                .sorted(Comparator.comparing(PriceDataPoint::getDate).reversed())
                .skip(1)
                .findFirst()
                .map(PriceDataPoint::getAdjClose);
    }


    @Override
    public String toString() {
        return "PriceHistory{" +
                "dataPoints=" + dataPoints +
                '}';
    }
}
