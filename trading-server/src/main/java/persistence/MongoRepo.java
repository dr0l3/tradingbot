package persistence;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import io.vavr.Tuple;
import model.*;
import model.dashboard.Dashboard;
import model.dashboard.DashboardEntry;
import model.dashboard.GameDates;
import model.selectors.SectorSelector;
import model.selectors.SingleCompanySelector;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MongoRepo implements Repo{
    private final Morphia morphia= new Morphia();

    private MongoClient client;
    private Datastore store;

    private PriceHistory priceHistory;



    private LoadingCache<Selector, List<String>> mappings = CacheBuilder.newBuilder()
            .maximumSize(3000)
            .build(
                    new CacheLoader<Selector, List<String>>() {
                        @Override
                        public List<String> load(Selector key) throws Exception {
                            Query<SectorInfo> query = store.createQuery(SectorInfo.class);
                            query.field("sector").containsIgnoreCase(((SectorSelector) key).getSectorName());
                            List<SectorInfo> res = query.asList();
                            System.out.println("numbers of symbolds for selector" + key.toString() + " : " + res.size());
                            return res.stream().map(SectorInfo::getSymbol)
                                    .map(s -> s.replaceAll("\\s+","")) //remove whitespace
                                    .collect(Collectors.toList());
                        }
                    }
            );

    public MongoRepo() {
        client = new MongoClient("mongo:27017");
        store =morphia.createDatastore(client, "example");
        morphia.mapPackage("model");
    }

    public MongoRepo(String address){
        client = new MongoClient(address);
        store =morphia.createDatastore(client, "example");
        morphia.mapPackage("model");
    }

    @Override
    public UserState addUser(UserState userState) {
        store.save(userState);
        return userState;
    }

    @Override
    public List<UserState> getAllUsers() {
        Query<UserState> query = store.createQuery(UserState.class);
        return query.asList();
    }

    @Override
    public UserState updateUser(UserState userState) {
        store.save(userState);
        return userState;
    }

    @Override
    public List<UserState> updateUsers(List<UserState> users) {
        store.save(users);
        return users;
    }

    @Override
    public Dashboard getCurrentStandings() {
        // TODO: 11/8/17 Progressbar
        List<UserState> users = getAllUsers();
        List<DashboardEntry> entries = users.stream()
                .map(u -> new DashboardEntry(u.getName(), u.getNetWorth()))
                .collect(Collectors.toList());
        return new Dashboard(0d, entries);
    }

    @Override
    public List<UserState> getLateUsers(LocalDate currentDate) {
        final Query<UserState> query = store.createQuery(UserState.class);
        query.criteria("stateComputedAt").lessThan(currentDate);
        return query.asList();
    }

    @Override
    public PriceHistory getPriceHistory(LocalDate date) {
        if(priceHistory == null){
            long start = System.nanoTime();
            final Query<PriceDataPoint> query = store.createQuery(PriceDataPoint.class);
            final java.util.List<PriceDataPoint> points = query.asList();
            java.util.List<String> symbols = points.stream()
                    .map(PriceDataPoint::getSymbol)
                    .distinct()
                    .collect(Collectors.toList());
            java.util.Map<String, List<PriceDataPoint>> pointsss = symbols.stream()
                    .map(symbol -> Tuple.of(symbol, points.stream()
                            .filter(p -> p.getSymbol().contentEquals(symbol)).collect(Collectors.toList())))
                    .collect(Collectors.toMap(t -> t._1, t -> t._2));
            System.out.println("Pricehistory fetch took: " + (System.nanoTime() - start));

            priceHistory =  new PriceHistory(pointsss);
            return priceHistory;
        } else{
            return priceHistory;
        }

    }

    @Override
    public List<String> getSymbolsForSelector(Selector selector) {

        if(selector instanceof SectorSelector) {
            try {
                return mappings.get(selector);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        if(selector instanceof SingleCompanySelector){
            return Lists.newArrayList(((SingleCompanySelector) selector).getSymbol());
        }
        return Lists.newArrayList();
    }

    @Override
    public LocalDate currentDate() {
        try{
            final Query<UserState> query = store.createQuery(UserState.class)
                    .order("-stateComputedAt");
            List<UserState> mostRecentlyUpdatedUser = query.asList(new FindOptions().limit(1));
            if(mostRecentlyUpdatedUser.isEmpty()){
                return LocalDate.of(2000,1,1);
            } else{
                return mostRecentlyUpdatedUser.get(0).getStateComputedAt();
            }
        } catch (Exception e){
            e.printStackTrace();
            return getGameDates().getStartDate();
        }
    }

    @Override
    public GameDates getGameDates() {
        final Query<GameDates> dateQuery = store.createQuery(GameDates.class);
        List<GameDates> dates = dateQuery.asList();
        if(dates.isEmpty())
            return new GameDates(LocalDate.of(2000,1,1), LocalDate.of(2010,1,1));
        else
            return  dates.get(0);
    }

    @Override
    public GameDates setGameDates(GameDates dates) {
        client.getDatabase("example").getCollection("gamedates").drop();
        store.save(dates);
        return dates;
    }
}
