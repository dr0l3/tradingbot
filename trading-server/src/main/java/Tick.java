import com.google.common.collect.Lists;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import model.*;
import model.dashboard.GameDates;
import org.apache.commons.collections4.ListUtils;
import org.bson.types.ObjectId;
import persistence.Repo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Tick {
    private Repo repo;

    public Tick(Repo repo) {
        this.repo = repo;
    }

    public Tick() {
    }

    public static void main(String[] args) {

    }

    public void catchup(LocalDate currentDate){
        GameDates dates = repo.getGameDates();
        List<UserState> late = repo.getAllUsers();
        List<UserState> updated = getUpdatedUsers(currentDate,late);
        late.removeAll(updated);
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


            for (Tuple3<LocalDate, List<ObjectId>,Double> up: updatesNeeded){
                LocalDate date = up._1;
                List<UserState> toUpdate = repo.getUsersById(up._2);
                Double maxProfitFactor = up._3;
                PriceHistory priceHistory = repo.getPriceHistory(date);

                List<UserState> newStates = calculateNewHoldings(toUpdate,priceHistory, date).join();
                //cap the winnings to be below the highest value as later comers have a slight advantage
                NetworthRecord record = repo.getHighestNetworth(date);
                newStates.forEach(state -> {
                    Double bound = (record.getNetworth() * maxProfitFactor) -1;
                    Double newNetworth = Math.min(bound,state.getNetWorth());
                    Double diff = newNetworth-bound;
                    Double adjustment = Math.max(0, diff);
                    Double afterAdjustment = state.getCapital() - adjustment;
                    state.setCapital(afterAdjustment);
                    state.setNetWorth(newNetworth);
                });
                repo.updateUsers(newStates);

            }
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
                    newStates.stream()
                            .sorted(Comparator.comparing(UserState::getNetWorth).reversed())
                            .findFirst()
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
            before.forEach(u -> u.setStateComputedAt(date));
            return CompletableFuture.completedFuture(before);
        }
        List<String> names = before.stream().map(ls -> ls.getName()).collect(Collectors.toList());
        long start = System.nanoTime();
        List<UserState> res = before.stream().map(state -> {
            //get total capital
            Double currentNetworth = calculateCostOfHoldings(priceHistory, state.getStrategies(), date) + state.getCapital();

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
                        return new UserState(state.getId(),strategiesWithExcess, capitalAfterTrading, state.getEmail(),state.getName(), networth, date, state.getInsertedAt());
                    }).orElseGet(() -> {
                        Double capitalAfterTrading = currentNetworth - calculateCostOfHoldings(priceHistory, desiredStrategies, date);
                        Double newNetworth = calculateCostOfHoldings(priceHistory,desiredStrategies,date) + capitalAfterTrading;
                        return new UserState(state.getId(),desiredStrategies, capitalAfterTrading, state.getEmail(), state.getName(), newNetworth, date, state.getInsertedAt());
                    });
        }).collect(Collectors.toList());

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
