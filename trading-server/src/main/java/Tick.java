import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import io.vavr.Tuple;
import model.*;
import model.selectors.SectorSelector;
import model.selectors.SingleCompanySelector;
import model.signals.AbsoluteValueSignal;
import org.apache.commons.collections4.ListUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Tick {
    private LocalDateTime startTime;
    private Morphia morphia;

    public Tick(Morphia morphia) {
        this.morphia = morphia;
    }

    public static void main(String[] args) {
        long start = System.nanoTime();
        Morphia morphia = new Morphia();
        morphia.mapPackage("model");
        Tick ticker = new Tick(morphia);
        LocalDate startDate = LocalDate.of(2014, 1, 1);
        LocalDate endDate = LocalDate.of(2014,12,31);
        LocalDate current = startDate;
        List<LocalDate> allDates = Lists.newArrayList();
        UserState simpleAbsolute = testState();
        UserState techUser = testTechUser();
        List<UserState> initialUsers = Lists.newArrayList();
        initialUsers.add(techUser);
        initialUsers.add(simpleAbsolute);

        ticker.persistUserStates(initialUsers);

        while(true){
            allDates.add(current);
            if(current.plusDays(1).isAfter(endDate)){
                break;
            }
            current = current.plusDays(1);
        }
        List<List<String>> dashBoards = Lists.newArrayList();
        allDates.forEach(date -> {
            long start2 = System.nanoTime();
            ticker.tick(date);
            System.out.printf("Tick took -> %d \n", (System.nanoTime() - start2));
            List<UserState> afterRound = ticker.getUserStates().join();
            PriceHistory priceHistory = ticker.getPriceHistory(date).join();
            dashBoards.add(printDashboard(afterRound,priceHistory));
        });

        dashBoards.forEach(db -> {
            System.out.println("----Dashboard----");
            db.forEach(System.out::println);
        });
        System.out.printf("Total time %d \n", (System.nanoTime()-start));

//        List<String> symbols = ticker.getSymbolsForSelector(new SectorSelector("Technology"));
//        symbols.forEach(System.out::println);
    }

    private static UserState testTechUser() {
        Double capital = 100000d;

        Selector lyg = new SectorSelector("Technology");
        Signal lygBuy = new AbsoluteValueSignal(4d, true, lyg);
        Signal lygSell = new AbsoluteValueSignal(90d, true, lyg);
        Integer lygPrio = 1;
        Double lygPercentage = 70d;

        UserStrategy strategy = new UserStrategy(lygBuy, lygSell, lyg, lygPrio, lygPercentage, Lists.newArrayList());


        List<UserStrategy> strategies = Lists.newArrayList();
        strategies.add(strategy);
        ObjectId id = new ObjectId();
        return new UserState(id,strategies, capital, "tech user");
    }

    private static List<String> printDashboard(List<UserState> users, PriceHistory history){
        return users.stream().map(user ->  {
           String name = user.getName();
           Double holdingsWorth = user.getStrategies().stream()
                   .flatMap(str -> str.getHoldings().stream())
                   .map(holding -> holding.getVolume() * history.getSalesPriceForSymbol(holding.getSymbol()).orElse(0d))
                   .reduce((a,b) -> a +b).orElse(0d);
           Double netWorth = user.getCapital() + holdingsWorth;
           List<Holding> allHoldings = user.getStrategies().stream().flatMap(str -> str.getHoldings().stream()).collect(Collectors.toList());

           return String.format("%s is worth -> %f with holdings %s",name,netWorth, allHoldings.toString());
        }).collect(Collectors.toList());
    }

    public static UserState testState() {
        Double capital = 100000d;

        Selector lyg = new SingleCompanySelector("LYG");
        Signal lygBuy = new AbsoluteValueSignal(4d, true, lyg);
        Signal lygSell = new AbsoluteValueSignal(90d, true, lyg);
        Integer lygPrio = 1;
        Double lygPercentage = 70d;

        UserStrategy strategy = new UserStrategy(lygBuy, lygSell, lyg, lygPrio, lygPercentage, Lists.newArrayList());

        Selector fet = new SingleCompanySelector("FET");
        Signal fetBuy = new AbsoluteValueSignal(32d, true, fet);
        Signal fetSell = new AbsoluteValueSignal(40d, true, fet);
        Integer fetPrio = 2;
        Double fetPercent = 20d;
        UserStrategy str2 = new UserStrategy(fetBuy, fetSell, fet, fetPrio, fetPercent, Lists.newArrayList());

        List<UserStrategy> strategies = Lists.newArrayList();
        strategies.add(strategy);
        strategies.add(str2);
        ObjectId id = new ObjectId();
        return new UserState(id,strategies, capital, "test1");
    }

    private void tick(LocalDate date) {
        getPriceHistory(date)
                .thenCompose(h -> getUserStates()
                        .thenApply(state -> Tuple.of(h, state)))
                .thenCompose(t -> {
                    List<UserState> userState = t._2;
                    PriceHistory hist = t._1;
                    return calculateNewHoldings(userState, hist);
                })
                .thenCompose(this::persistUserStates)
                .exceptionally(v -> CompletableFuture.runAsync(() -> {
                    System.out.println(v.getMessage());
                    v.printStackTrace();
                }).join());
    }

    private CompletableFuture<Void> persistUserStates(List<UserState> newState) {
        long start = System.nanoTime();
        MongoClient client = new MongoClient("localhost:32768");
        final Datastore store = morphia.createDatastore(client, "example");
        store.save(newState);
        System.out.printf("Persisting changes took %d \n",(System.nanoTime()-start));
        return CompletableFuture.runAsync(()->newState
                .forEach(v ->v.getStrategies()
                        .forEach(st ->System.out.println(st.getHoldings().toString())))); // TODO: 10/12/17 IMplement
}

    private CompletableFuture<List<UserState>> getUserStates() {
        List<UserState> states = Lists.newArrayList();
        MongoClient client = new MongoClient("localhost:32768");
        final Datastore store = morphia.createDatastore(client, "example");
        Query<UserState> query = store.createQuery(UserState.class);
        states = query.asList();

        return CompletableFuture.completedFuture(states); // TODO: 10/12/17 Implement
    }

    public CompletableFuture<List<UserState>> calculateNewHoldings(List<UserState> before, PriceHistory priceHistory) {
        long start = System.nanoTime();
        List<UserState> res = before.stream().map(state -> {
            //get total capital
            Double currentCapital = valueOfHoldingsToday(priceHistory, state.getStrategies()) + state.getCapital();

            //calculate optimal holding for current capital and market conditions
            List<UserStrategy> desiredStrategies = state.getStrategies().stream().map(strategy -> {
                //how much cash can we spend on this strategy?
                Double allotedCapital = currentCapital * strategy.getPercantage() / 100;
                List<Holding> volumes;


                if (strategy.getBuySignal().isActive(priceHistory) || !strategy.getHoldings().isEmpty()) {
                    //how many different stocks do we need to spread our money over?
                    Integer holdingNumber = strategy.getSelector().matchedSymbols(priceHistory).size();
                    //calculate the volume for each
                    volumes = strategy.getSelector().matchedSymbols(priceHistory).stream()
                            .map(symbol -> {
                                Double volume = priceHistory.getSalesPriceForSymbol(symbol)
                                        .map(price -> allotedCapital / holdingNumber / price)
                                        .orElse(0d);
                                return new Holding(symbol, volume);
                            }).collect(Collectors.toList());
                } else {
                    volumes = Lists.newArrayList();
                }
                System.out.println(volumes.size());
                List<Holding> nonEmpty = volumes.stream().filter(h -> h.getVolume() > 0.0).collect(Collectors.toList());
                System.out.println(nonEmpty.size());
                return new UserStrategy(strategy.getBuySignal(), strategy.getSellSignal(), strategy.getSelector(), strategy.getPriority(), strategy.getPercantage(), nonEmpty);
            }).collect(Collectors.toList());

            Double excessCapital = currentCapital - calculateCostOfHoldings(priceHistory, desiredStrategies);

            return desiredStrategies.stream()
                    .filter(strategy -> strategy.getBuySignal().isActive(priceHistory))
                    .sorted(Comparator.comparing(UserStrategy::getPriority))
                    .findFirst()
                    .map(excessStrategy -> {
                        List<String> symbols = excessStrategy.getSelector().matchedSymbols(priceHistory);
                        List<Holding> excessHoldsings = symbols.stream()
                                .map(symbol -> {
                                    Double volume = priceHistory.getSalesPriceForSymbol(symbol)
                                            .map(price -> excessCapital / symbols.size() / price)
                                            .orElse(0d);
                                    return new Holding(symbol, volume);
                                })
                                .filter(h -> h.getVolume() > 0)
                                .collect(Collectors.toList());

                        List<Holding> allHoldings = ListUtils.union(excessHoldsings, excessStrategy.getHoldings());

                        UserStrategy highPrio = new UserStrategy(excessStrategy.getBuySignal()
                                , excessStrategy.getSellSignal()
                                , excessStrategy.getSelector()
                                , excessStrategy.getPriority()
                                , excessStrategy.getPercantage()
                                , allHoldings);


                        List<UserStrategy> strategiesWithExcess = Lists.newArrayList(desiredStrategies);
                        strategiesWithExcess.remove(excessStrategy);
                        strategiesWithExcess.add(highPrio);
                        Double costOfHoldings = calculateCostOfHoldings(priceHistory, strategiesWithExcess);
                        Double capitalAfterTrading = currentCapital - costOfHoldings;

                        return new UserState(state.getId(),strategiesWithExcess, capitalAfterTrading, state.getName());
                    }).orElseGet(() -> {
                        Double capitalAfterTrading = currentCapital - calculateCostOfHoldings(priceHistory, desiredStrategies);
                        return new UserState(state.getId(),desiredStrategies, capitalAfterTrading, state.getName());
                    });
        }).collect(Collectors.toList());

        System.out.printf("Calculating new state took %d \n", (System.nanoTime() - start));
        return CompletableFuture.completedFuture(res);
    }

    private Double calculateCostOfHoldings(PriceHistory priceHistory, List<UserStrategy> strategies) {
        return strategies.stream()
                .flatMap(strategy -> strategy.getHoldings().stream())
                .map(holding -> holding.getVolume() * priceHistory.getSalesPriceForSymbol(holding.getSymbol())
                        .orElse(0d))
                .reduce((a, b) -> a + b)
                .orElse(0d);
    }

    private CompletableFuture<PriceHistory> getPriceHistory(LocalDate date) {
        MongoClient client = new MongoClient("localhost:32768");
        final Datastore store = morphia.createDatastore(client, "example");
        final Query<PriceDataPoint> query = store.createQuery(PriceDataPoint.class);
        query.criteria("date").greaterThanOrEq(date.minusDays(9)).add(query.criteria("date").lessThanOrEq(date.plusDays(1)));
        final java.util.List<PriceDataPoint> points = query.asList();
        java.util.List<String> symbols = points.stream()
                .map(PriceDataPoint::getSymbol)
                .distinct()
                .collect(Collectors.toList());
        System.out.println(symbols);
        java.util.Map<String, List<PriceDataPoint>> pointsss = symbols.stream()
                .map(symbol -> Tuple.of(symbol, points.stream()
                        .filter(p -> p.getSymbol().contentEquals(symbol)).collect(Collectors.toList())))
                .collect(Collectors.toMap(t -> t._1, t -> t._2));
        return CompletableFuture.completedFuture(new PriceHistory(pointsss)); // TODO: 10/12/17 Get price history
    }

    private Double calculateGainsByTrading(PriceHistory priceHistory, List<UserStrategy> before, List<UserStrategy> after) {
        Double yesterday = valueOfHoldingsYesterday(priceHistory, before);
        Double today = valueOfHoldingsToday(priceHistory, after);

        return yesterday - today;
    }

    private Double valueOfHoldingsYesterday(PriceHistory priceHistory, List<UserStrategy> before) {
        return before.stream()
                .flatMap(str -> str.getHoldings().stream()
                .map(h -> priceHistory.getSalesPriceForSymbolYesterday(h.getSymbol())
                        .orElse(0d) * h.getVolume()))
                .reduce((a, b) -> a + b)
                .orElse(0d);
    }

    private Double valueOfHoldingsToday(PriceHistory priceHistory, List<UserStrategy> after) {
        return after.stream()
                .flatMap(str -> str.getHoldings().stream()
                .map(h -> priceHistory.getSalesPriceForSymbol(h.getSymbol())
                        .orElse(0d) * h.getVolume()))
                .reduce((a, b) -> a + b)
                .orElse(0d);
    }
}
