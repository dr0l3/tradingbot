package model;


import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import model.selectors.SectorSelector;
import model.selectors.SingleCompanySelector;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PriceHistory {
    private Map<String,List<PriceDataPoint>> dataPoints;

    public PriceHistory(Map<String, List<PriceDataPoint>> dataPoints) {
        this.dataPoints = dataPoints;
    }


    public Optional<Double> getSalesPriceForSymbol(String symbol){
        return dataPoints.getOrDefault(symbol, Lists.newArrayList()).stream()
                .sorted(Comparator.comparing(PriceDataPoint::getDate))
                .findFirst()
                .map(PriceDataPoint::getClose);
    }

    public Optional<Double> getSalesPriceForSymbolYesterday(String symbol){
        return dataPoints.getOrDefault(symbol, Lists.newArrayList()).stream()
                .sorted(Comparator.comparing(PriceDataPoint::getDate))
                .skip(1)
                .findFirst()
                .map(PriceDataPoint::getClose);
    }

    public List<String> getSymbolsForSelector(Selector selector){
        Morphia morphia = new Morphia();
        morphia.mapPackage("model");
        MongoClient client = new MongoClient("localhost:32768");
        final Datastore store = morphia.createDatastore(client, "example");

        if(selector instanceof SectorSelector) {
            Query<SectorInfo> query = store.createQuery(SectorInfo.class);
            query.field("sector").containsIgnoreCase(((SectorSelector) selector).getSectorName());
            List<SectorInfo> res = query.asList();
            return res.stream().map(SectorInfo::getSymbol).collect(Collectors.toList());
        }
        if(selector instanceof SingleCompanySelector){
            return Lists.newArrayList(((SingleCompanySelector) selector).getSymbol());
        }
        return Lists.newArrayList();
    }

    @Override
    public String toString() {
        return "PriceHistory{" +
                "dataPoints=" + dataPoints +
                '}';
    }
}
