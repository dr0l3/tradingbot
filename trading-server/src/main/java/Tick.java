import com.google.common.collect.Lists;
import io.vavr.Tuple;
import io.vavr.Tuple2;
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
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
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

    public static void main(String[] args) {
//        Gson gson = new GsonBuilder()
//                .registerTypeAdapter(Selector.class, new GsonInterfaceAdapter<Selector>())
//                .registerTypeAdapter(Signal.class, new GsonInterfaceAdapter<Signal>())
//                .create();
//
//        String json = "[{\"selector\":{\"type\":\"model.selectors.SingleCompanySelector\",\"data\":{\"symbol\":\"PJC\"}},\"buySignal\":{\"type\":\"model.signals.ConstantSignal\",\"data\":{\"isAlwaysActive\":true}},\"sellSignal\":{\"type\":\"model.signals.ConstantSignal\",\"data\":{\"isAlwaysActive\":false}}}]";
//
//        UserState s = gson.fromJson(json,UserState.class);
//        System.out.println(s);

        Repo repo = new MongoRepo("localhost:32768");
        Selector sel = new SectorSelector("Technology");

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
                   .map(holding -> holding.getVolume() * history.getClosePriceForSymbol(holding.getSymbol(), date).orElse(0d))
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
        List<UserState> late = repo.getAllUsers();
        List<UserState> updated = getUpdatedUsers(currentDate,late);
        late.removeAll(updated);
        System.out.println("Late users for date " + currentDate.toString());
        if(late.size() > 0){
            List<LocalDate> replayDates = getDatesBetween(startDate,currentDate);
            List<Tuple2<LocalDate, List<UserState>>> updatesNeeded = replayDates.stream().map(replayDate -> {
                List<UserState> users = late.stream()
                        .filter(u -> u.getStateComputedAt().isBefore(replayDate))
                        .collect(Collectors.toList());
                return Tuple.of(replayDate,users);
            }).collect(Collectors.toList());

            updatesNeeded.forEach(t -> {
                LocalDate time =t._1;
                List<String> usersName = t._2.stream().map(UserState::getName).collect(Collectors.toList());
                System.out.println("currentDate " + currentDate.toString() +" Time " + time.toString() + " users: " + usersName);
            });

            List<UserState> usersss = Lists.newArrayList();
            for (Tuple2<LocalDate, List<UserState>> up: updatesNeeded){
                LocalDate date = up._1;
                usersss = up._2;
                PriceHistory priceHistory = repo.getPriceHistory(date);
                //all new ones
                List<UserState> newStates = calculateNewHoldings(usersss,priceHistory, date).join();
                //their ids
                List<ObjectId> newStateIds = newStates.stream().map(UserState::getId).collect(Collectors.toList());
                //remove new ones
                List<UserState> old = usersss.stream().filter(us -> !newStateIds.contains(us.getId())).collect(Collectors.toList());
                usersss = ListUtils.union(old,newStates);
            }
            // TODO: 11/6/17 Capping of users
            repo.updateUsers(usersss);
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
                            List<UserState> up2dateUsers = getUpdatedUsers(currentDate, state);
                            List<String> names = up2dateUsers.stream().map(n -> n.getName()).collect(Collectors.toList());
                            System.out.println("Updated users for " + currentDate.toString() + " users: " + names);
                            return Tuple.of(history, up2dateUsers);
                        }))
                .thenCompose(t -> {
                    System.out.println("Calculating holdings for date " + currentDate.toString());
                    List<UserState> userState = t._2;
                    PriceHistory hist = t._1;
                    return calculateNewHoldings(userState, hist, currentDate);
                })
                .thenCompose(v -> {
                    System.out.println("Updating users for date " + currentDate.toString());
                    return CompletableFuture.supplyAsync(() -> {repo.updateUsers(v); return v;});})
                .exceptionally(v -> CompletableFuture.supplyAsync(() -> {
                    System.out.println(v.getMessage());
                    v.printStackTrace();
                    return repo.getAllUsers();
                }).join()).join();
    }

    private List<UserState> getUpdatedUsers(LocalDate currentDate, List<UserState> state) {
        return state.stream()
                .filter(u -> u.getStateComputedAt()
                        .isAfter(currentDate.minusDays(2)))
                .collect(Collectors.toList());
    }

    private List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate currentDate) {
        List<LocalDate> replayDates = Lists.newArrayList();


        LocalDate current = startDate;
        while(true){
            replayDates.add(current);
            if(current.plusDays(1).isAfter(currentDate)){
                break;
            }
            current = current.plusDays(1);
        }

        return replayDates;
    }


    public CompletableFuture<List<UserState>> calculateNewHoldings(List<UserState> before, PriceHistory priceHistory, LocalDate date) {
        List<String> names = before.stream().map(ls -> ls.getName()).collect(Collectors.toList());
        System.out.println("Calculating new holdings for date " + date.toString() + " for users " + names);
        long start = System.nanoTime();
        List<UserState> res = before.stream().map(state -> {
            //get total capital
            Double currentNetworth = calculateCostOfHoldingsOpen(priceHistory, state.getStrategies(), date) + state.getCapital();
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
                                Double volume = priceHistory.getOpenPriceForSymbol(symbol, date)
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

            Double excessCapital = currentNetworth - calculateCostOfHoldingsOpen(priceHistory, desiredStrategies, date);

            return desiredStrategies.stream()
                    .filter(strategy -> strategy.isActive(priceHistory, repo, date))
                    .sorted(Comparator.comparing(UserStrategy::getPriority))
                    .findFirst()
                    .map(excessStrategy -> {
                        List<String> symbols = excessStrategy.getSelector().matchedSymbols(priceHistory, repo);
                        List<Holding> excessHoldsings = symbols.stream()
                                .map(symbol -> {
                                    Double volume = priceHistory.getOpenPriceForSymbol(symbol, date)
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
                        Double costOfHoldings = calculateCostOfHoldingsOpen(priceHistory, strategiesWithExcess, date);
                        Double capitalAfterTrading = currentNetworth - costOfHoldings;
                        Double newNetworth = calculateCostOfHoldingsClose(priceHistory,strategiesWithExcess,date) + capitalAfterTrading;
                        Double diff = calculateCostOfHoldingsClose(priceHistory,strategiesWithExcess, date) - costOfHoldings;
                        Double diff2 = calculateCostOfHoldingsOpen(priceHistory,strategiesWithExcess,date.plusDays(1)) - costOfHoldings;
                        System.out.println("User: " + state.getName() + " Capital after trading " + capitalAfterTrading + " cost of holdings " + costOfHoldings + " newnetworth " + newNetworth + " Money made from holdings today " + diff + " money made from holdings tomorrow " +diff2);
                        return new UserState(state.getId(),strategiesWithExcess, capitalAfterTrading, state.getName(), newNetworth, date, state.getInsertedAt());
                    }).orElseGet(() -> {
                        Double capitalAfterTrading = currentNetworth - calculateCostOfHoldingsOpen(priceHistory, desiredStrategies, date);
                        Double newNetworth = calculateCostOfHoldingsClose(priceHistory,desiredStrategies,date) + capitalAfterTrading;
                        return new UserState(state.getId(),desiredStrategies, capitalAfterTrading, state.getName(), newNetworth, date, state.getInsertedAt());
                    });
        }).collect(Collectors.toList());

//        System.out.printf("Calculating new state took %d \n", (System.nanoTime() - start));
        return CompletableFuture.completedFuture(res);
    }

    private Double calculateCostOfHoldingsClose(PriceHistory priceHistory, List<UserStrategy> strategies, LocalDate date) {
        return strategies.stream()
                .flatMap(strategy -> strategy.getHoldings().stream())
                .map(holding -> holding.getVolume() * priceHistory.getClosePriceForSymbol(holding.getSymbol(), date)
                        .orElse(0d))
                .reduce((a, b) -> a + b)
                .orElse(0d);
    }




    private Double calculateCostOfHoldingsOpen(PriceHistory priceHistory, List<UserStrategy> after, LocalDate date) {
        return after.stream()
                .flatMap(str -> str.getHoldings().stream()
                .map(h -> priceHistory.getOpenPriceForSymbol(h.getSymbol(), date)
                        .orElse(0d) * h.getVolume()))
                .reduce((a, b) -> a + b)
                .orElse(0d);
    }
}
