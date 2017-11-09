import com.google.common.collect.Lists;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import model.*;
import model.dashboard.Dashboard;
import model.dashboard.DashboardEntry;
import model.dashboard.GameDates;
import model.selectors.SectorSelector;
import model.selectors.SingleCompanySelector;
import model.signals.PriceSignal;
import org.apache.commons.collections4.ListUtils;
import org.bson.types.ObjectId;
import persistence.MongoRepo;
import persistence.Repo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Tick {
    private static LocalDate startDate;
    private static LocalDate currentDate;
    private Repo repo;
    private PriceHistory priceHistory;

    public Tick(Repo repo) {
        this.repo = repo;
    }

    public Tick() {
    }

    public static void main(String[] args) {
//        Tick t = new Tick();
//        Gson gson = new GsonBuilder()
//                .registerTypeAdapter(Selector.class, new GsonInterfaceAdapter<Selector>())
//                .registerTypeAdapter(Signal.class, new GsonInterfaceAdapter<Signal>())
//                .create();
//
//        String json = "[{\"selector\":{\"type\":\"model.selectors.SingleCompanySelector\",\"data\":{\"symbol\":\"PJC\"}},\"buySignal\":{\"type\":\"model.signals.ConstantSignal\",\"data\":{\"isAlwaysActive\":true}},\"sellSignal\":{\"type\":\"model.signals.ConstantSignal\",\"data\":{\"isAlwaysActive\":false}}}]";
//
//        UserState s = gson.fromJson(json,UserState.class);
//        System.out.println(s);

//        Repo repo = new MongoRepo("localhost:32768");
//        Selector sel = new SectorSelector("Technology");
//


//        runExperiment();

    }

    public static Dashboard randomDb(){
        Random r = new Random();
        DashboardEntry dbE = new DashboardEntry(UUID.randomUUID().toString(), r.nextDouble()*100000);
        return new Dashboard(0d,Lists.newArrayList(dbE));
    }

    public static void runExperiment() {
        long start = System.nanoTime();
        MongoRepo repo = new MongoRepo("localhost:32768");
        Tick ticker = new Tick(repo);
        startDate = LocalDate.of(2014, 1, 6);
        LocalDate endDate = LocalDate.of(2014,1,7);
        LocalDate current = startDate;
        List<LocalDate> allDates = Lists.newArrayList();
        UserState simpleAbsolute = testState();
        UserState techUser = testTechUser("tech user",startDate);
        List<UserState> initialUsers = Lists.newArrayList();
        initialUsers.add(techUser);
        initialUsers.add(simpleAbsolute);

        repo.updateUsers(initialUsers);

        GameDates gameDates = new GameDates(startDate,endDate);
        repo.setGameDates(gameDates);

        while(true){
            allDates.add(current);
            if(current.plusDays(1).isAfter(endDate)){
                break;
            }
            current = current.plusDays(1);
        }

        List<List<String>> dashBoards = Lists.newArrayList();
        List<List<LocalDate>> partitioned = Lists.partition(allDates, 20);
        partitioned.forEach(dates -> {
            dates.forEach(date -> {
                long start2 = System.nanoTime();
//                repo.addUser(testTechUser("techuser" + date.toString(), date.minusDays(2)));
//                List<UserState> state = ticker.tick(date);
//                ticker.catchup(date);
//                System.out.printf("Tick took for date %s -> %d \n", date.toString(), (System.nanoTime() - start2));
//                List<UserState> afterRound = repo.getAllUsers();
//                PriceHistory priceHistory = repo.getPriceHistory(date);
//                dashBoards.add(printDashboard(afterRound,priceHistory, date));
            });
        });



        dashBoards.forEach(db -> {
            System.out.println("----Dashboard----");
            db.forEach(System.out::println);
        });
        System.out.printf("Total time %d \n", (System.nanoTime()-start));

//        List<String> symbols = ticker.getSymbolsForSelector(new SectorSelector("Technology"));
//        symbols.forEach(System.out::println);
    }

    private static UserState testTechUser(String name, LocalDate date) {
        Double capital = 100000d;

        Selector lyg = new SectorSelector("Technology");
        Signal lygBuy = new PriceSignal(4d, true);
        Signal lygSell = new PriceSignal(90d, true);
        Integer lygPrio = 1;
        Double lygPercentage = 70d;

        UserStrategy strategy = new UserStrategy(lygBuy, lygSell, lyg, lygPrio, lygPercentage, Lists.newArrayList());


        List<UserStrategy> strategies = Lists.newArrayList();
        strategies.add(strategy);
        ObjectId id = new ObjectId();
        return new UserState(id,strategies, capital, name, capital, startDate.minusDays(1), startDate.minusDays(1));
    }

    private static List<String> printDashboard(List<UserState> users, PriceHistory history, LocalDate date){
        return users.stream().map(user ->  {
           String name = user.getName();
           Double holdingsWorth = user.getStrategies().stream()
                   .flatMap(str -> str.getHoldings().stream())
                   .map(holding -> holding.getVolume() * history.getPriceForSymbol(holding.getSymbol(), date).orElse(0d))
                   .reduce((a,b) -> a +b).orElse(0d);
           Double netWorth = user.getCapital() + holdingsWorth;
           List<Holding> allHoldings = user.getStrategies().stream().flatMap(str -> str.getHoldings().stream()).collect(Collectors.toList());

           return String.format("%s is worth -> %f with capital %f and holdings %s",name,netWorth,user.getCapital(), allHoldings.toString());
        }).collect(Collectors.toList());
    }

    public static UserState testState() {
        Double capital = 100000d;
        LocalDate insertedAt = startDate;

        Selector lyg = new SingleCompanySelector("LYG");
        Signal lygBuy = new PriceSignal(5.30d, true);
        Signal lygSell = new PriceSignal(5.60d, true);
        Integer lygPrio = 1;
        Double lygPercentage = 70d;

        UserStrategy strategy = new UserStrategy(lygBuy, lygSell, lyg, lygPrio, lygPercentage, Lists.newArrayList());

        Selector fet = new SingleCompanySelector("FET");
        Signal fetBuy = new PriceSignal(32d, true);
        Signal fetSell = new PriceSignal(40d, true);
        Integer fetPrio = 2;
        Double fetPercent = 20d;
        UserStrategy str2 = new UserStrategy(fetBuy, fetSell, fet, fetPrio, fetPercent, Lists.newArrayList());

        List<UserStrategy> strategies = Lists.newArrayList();
        strategies.add(strategy);
        strategies.add(str2);
        ObjectId id = new ObjectId();
        return new UserState(id,strategies, capital, "test1", capital, insertedAt, insertedAt);
    }

    public void catchup(LocalDate currentDate){
        System.out.println("Catchup for date " + currentDate.toString());
        GameDates dates = repo.getGameDates();
        List<UserState> late = repo.getAllUsers();
        List<UserState> updated = getUpdatedUsers(currentDate,late);
        late.removeAll(updated);
        late.forEach(l -> System.out.println(l.getName()));
        if(late.size() > 0){
            List<LocalDate> replayDates = getDatesBetweenExclusive(dates.getStartDate(),currentDate);
            replayDates.add(currentDate);
            double totalDays = (double) ChronoUnit.DAYS.between(dates.getStartDate(),currentDate);
            List<Tuple3<LocalDate, List<ObjectId>, Double>> updatesNeeded = replayDates.stream().map(replayDate -> {
                List<ObjectId> users = late.stream()
                        .filter(u -> u.getStateComputedAt().isBefore(replayDate))
                        .map(UserState::getId)
                        .collect(Collectors.toList());
                Double progress =  totalDays == 0 ? 0 : ChronoUnit.DAYS.between(dates.getStartDate(),replayDate) / totalDays;
                Double profitCap = 2d-progress;
                return Tuple.of(replayDate,users, profitCap);
            }).collect(Collectors.toList());

            updatesNeeded.forEach(t -> {
                LocalDate time =t._1;
                System.out.println("currentDate " + currentDate.toString() +" Time " + time.toString() + " users: " + t._2 + " profit cap" + t._3);
            });
            System.out.println("???????");


            for (Tuple3<LocalDate, List<ObjectId>,Double> up: updatesNeeded){
                LocalDate date = up._1;
                List<UserState> toUpdate = repo.getUsersById(up._2);
                Double maxProfitFactor = up._3;
                PriceHistory priceHistory = repo.getPriceHistory(date);
                System.out.println("!!!!!!!!!!");
                System.out.println(date);
                System.out.println(up._2);

                List<UserState> newStates = calculateNewHoldings(toUpdate,priceHistory, date).join();
                //cap the winnings to be below the highest value as later comers have a slight advantage
                NetworthRecord record = repo.getHighestNetworth(date);
                System.out.println("]]]]]]]]]]]]]]]]]]]]]]]]]]]");
                System.out.println(record);
                System.out.println("[[[[[[[[[[[[[[[[[[[[[[[[[[");
                newStates.forEach(state -> {
                    System.out.println(state.getNetWorth());
                    System.out.println(record);
                    System.out.println(record.getNetworth());
                    System.out.println(maxProfitFactor);
                    Double bound = record.getNetworth() * maxProfitFactor;
                    System.out.println(bound);
                    Double newNetworth = Math.min(bound,state.getNetWorth());
                    System.out.println(newNetworth);
                    Double diff = newNetworth-bound;
                    System.out.println(diff);
                    Double adjustment = Math.max(0, diff);
                    System.out.println(adjustment);
                    Double afterAdjustment = state.getCapital() - adjustment;
                    state.setCapital(afterAdjustment);
                    state.setNetWorth(newNetworth);
                    System.out.println(state.getNetWorth());
                    System.out.println("&&&&&&&&&&&&&&");
                });
                repo.updateUsers(newStates);

            }
            System.out.println();
            System.out.println("//////////");
        }
    }

    public void prepare(){

        System.out.println("Loading users");
        List<UserState> users = repo.getAllUsers();
        System.out.println("Loading startdate");
        LocalDate startDate = repo.getGameDates().getStartDate();
        System.out.println("Loading price data");
        repo.getPriceHistory(startDate);
        users.forEach(u -> u.setStateComputedAt(startDate));
        repo.updateUsers(users);
    }

    public List<UserState> tick(LocalDate currentDate) {
        return CompletableFuture.supplyAsync(() -> repo.getPriceHistory(currentDate))
                .thenCompose(history -> CompletableFuture.supplyAsync(() -> repo.getAllUsers())
                        .thenApply(state -> {
                            List<UserState> up2dateUsers = getUpdatedUsers(currentDate, state).stream()
                                    .filter(v -> v.getInsertedAt().isBefore(currentDate))
                                    .collect(Collectors.toList());
                            return Tuple.of(history, up2dateUsers);
                        }))
                .thenCompose(t -> {
                    List<UserState> userState = t._2;
                    PriceHistory hist = t._1;
                    return calculateNewHoldings(userState, hist, currentDate);
                })
                .thenCompose(newStates -> {
                    Optional<UserState> opt =newStates.stream()
                            .sorted(Comparator.comparing(UserState::getNetWorth))
                            .findFirst();
                    System.out.println(opt);
                    opt
                            .ifPresent(userState -> repo.setHighestNetworthForDate(new NetworthRecord(currentDate, Math.max(100000d,userState.getNetWorth()))));
                    return CompletableFuture.supplyAsync(() -> {repo.updateUsers(newStates); return newStates;});})
                .exceptionally(v -> CompletableFuture.supplyAsync(() -> {
                    System.out.println(v.getMessage());
                    v.printStackTrace();
                    return repo.getAllUsers();
                }).join()).join();
    }

    private List<UserState> getUpdatedUsers(LocalDate currentDate, List<UserState> state) {
        LocalDate lastTradingdate = repo.lastTradingDate(currentDate);
        return state.stream()
                .filter(u -> u.getStateComputedAt().isAfter(lastTradingdate.minusDays(1)))
                .collect(Collectors.toList());
    }

    /**
     * Gets dates between two dates, including the first date, excluding the second date.
     *
     * @param startDate
     * @param currentDate
     * @return
     */
    private List<LocalDate> getDatesBetweenExclusive(LocalDate startDate, LocalDate currentDate) {
        List<LocalDate> replayDates = Lists.newArrayList();


        LocalDate current = startDate;
        while(true){
            if(current.plusDays(1).isAfter(currentDate)){
                break;
            }
            replayDates.add(current);
            current = current.plusDays(1);
        }

        return replayDates;
    }


    public CompletableFuture<List<UserState>> calculateNewHoldings(List<UserState> before, PriceHistory priceHistory, LocalDate date) {
        if(!repo.isTradingDate(date)){
            System.out.println(date.toString() + " is not a trading date");
            before.forEach(u -> u.setStateComputedAt(date));
            return CompletableFuture.completedFuture(before);
        }
        List<String> names = before.stream().map(ls -> ls.getName()).collect(Collectors.toList());
        System.out.println("Calculating new holdings for date " + date.toString() + " for users " + names);
        long start = System.nanoTime();
        List<UserState> res = before.stream().map(state -> {
            //get total capital
            Double currentNetworth = calculateCostOfHoldings(priceHistory, state.getStrategies(), date) + state.getCapital();
            System.out.println(currentNetworth);

            //calculate optimal holding for current capital and market conditions
            List<UserStrategy> desiredStrategies = state.getStrategies().stream().map(strategy -> {
                //how much cash can we spend on this strategy?
                Double allotedCapital = currentNetworth * strategy.getPercantage() / 100;
                List<Holding> volumes;

                if (strategy.isActive(priceHistory, repo, date) || !strategy.getHoldings().isEmpty()) {
                    //how many different stocks do we need to spread our money over?
                    Integer holdingNumber = strategy.getSelector().matchedSymbols(priceHistory, repo).size();
                    //calculate the volume for each
                    volumes = strategy.activeSymbols(priceHistory, repo, date).stream()
                            .map(symbol -> {
                                Double volume = priceHistory.getPriceForSymbol(symbol, date)
                                        .map(price -> allotedCapital / holdingNumber / price)
                                        .orElse(0d);
                                return new Holding(symbol, volume);
                            }).collect(Collectors.toList());
                } else {
                    volumes = Lists.newArrayList();
                }
                List<Holding> nonEmpty = volumes.stream().filter(h -> h.getVolume() > 0.0).collect(Collectors.toList());
                return new UserStrategy(strategy.getBuySignal(), strategy.getSellSignal(), strategy.getSelector(), strategy.getPriority(), strategy.getPercantage(), nonEmpty);
            }).collect(Collectors.toList());

            Double excessCapital = currentNetworth - calculateCostOfHoldings(priceHistory, desiredStrategies, date);

            return desiredStrategies.stream()
                    .filter(strategy -> strategy.isActive(priceHistory, repo, date))
                    .sorted(Comparator.comparing(UserStrategy::getPriority))
                    .findFirst()
                    .map(excessStrategy -> {
                        List<String> symbols = excessStrategy.getSelector().matchedSymbols(priceHistory, repo);
                        List<Holding> excessHoldsings = symbols.stream()
                                .map(symbol -> {
                                    Double volume = priceHistory.getPriceForSymbol(symbol, date)
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
                        Double costOfHoldings = calculateCostOfHoldings(priceHistory, strategiesWithExcess, date);
                        Double capitalAfterTrading = currentNetworth - costOfHoldings;
                        Double networth = calculateCostOfHoldings(priceHistory,strategiesWithExcess,date) + capitalAfterTrading;
                        System.out.println("User: " + state.getName() + " Capital after trading " + capitalAfterTrading + " cost of holdings " + costOfHoldings + " newnetworth " + networth);
                        return new UserState(state.getId(),strategiesWithExcess, capitalAfterTrading, state.getName(), networth, date, state.getInsertedAt());
                    }).orElseGet(() -> {
                        Double capitalAfterTrading = currentNetworth - calculateCostOfHoldings(priceHistory, desiredStrategies, date);
                        Double newNetworth = calculateCostOfHoldings(priceHistory,desiredStrategies,date) + capitalAfterTrading;
                        return new UserState(state.getId(),desiredStrategies, capitalAfterTrading, state.getName(), newNetworth, date, state.getInsertedAt());
                    });
        }).collect(Collectors.toList());

//        System.out.printf("Calculating new state took %d \n", (System.nanoTime() - start));
        return CompletableFuture.completedFuture(res);
    }




    private Double calculateCostOfHoldings(PriceHistory priceHistory, List<UserStrategy> after, LocalDate date) {
        return after.stream()
                .flatMap(str -> str.getHoldings().stream()
                .map(h -> priceHistory.getPriceForSymbol(h.getSymbol(), date)
                        .orElse(0d) * h.getVolume()))
                .reduce((a, b) -> a + b)
                .orElse(0d);
    }
}
