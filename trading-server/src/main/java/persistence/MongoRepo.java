package persistence;

import REST.CompanyTuple;
import REST.InitialCompanyPrice;
import REST.InitialSectorPrice;
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
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MongoRepo implements Repo {
    private final Morphia morphia = new Morphia();

    private MongoClient client;
    private Datastore store;

    private PriceHistory priceHistory;

    private LoadingCache<String, InitialCompanyPrice> initialCompanyPriceCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<String, InitialCompanyPrice>() {
                @Override
                public InitialCompanyPrice load(String symbol) throws Exception {
                    LocalDate date = getGameDates().getStartDate();
                    Double price;
                    if (priceHistory == null) {
                        Query<PriceDataPoint> query = store.createQuery(PriceDataPoint.class);
                        query.criteria("symbol").containsIgnoreCase(symbol);
                        List<PriceDataPoint> dataPoints = query.asList().stream()
                                .sorted(Comparator.comparing(PriceDataPoint::getDate))
                                .collect(Collectors.toList());
                        System.out.println(dataPoints);
                        price = dataPoints.isEmpty() ? 0d : dataPoints.get(0).getAdjClose();
                    } else {
                        price = priceHistory.getPriceForSymbol(symbol, date).orElse(0d);
                    }

                    return new InitialCompanyPrice(symbol, price);
                }
            });

    private LoadingCache<String, InitialSectorPrice> sectorPriceCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .build(new CacheLoader<String, InitialSectorPrice>() {
                @Override
                public InitialSectorPrice load(String sector) throws Exception {
                    LocalDate startDate = getGameDates().getStartDate();
                    List<String> symbolsForSector = getCompanyTupleBySector(sector).stream()
                            .map(CompanyTuple::getSymbol)
                            .collect(Collectors.toList());
                    //get earliestPrice

                    System.out.println(startDate);
                    List<Double> prices = symbolsForSector.stream()
                            .map(s -> {
                                Query<PriceDataPoint> query = store.createQuery(PriceDataPoint.class);
                                System.out.println(s);
                                query.criteria("symbol").containsIgnoreCase(s)
                                        .and(query.criteria("date").lessThanOrEq(startDate.plusDays(2)));
                                List<PriceDataPoint> dataPoints = query.asList().stream()
                                        .sorted(Comparator.comparing(PriceDataPoint::getDate))
                                        .collect(Collectors.toList());
                                if(dataPoints.isEmpty() || dataPoints.get(0) == null || dataPoints.get(0).getAdjClose() == null)
                                    return Optional.empty();
                                else{
                                    return Optional.of(dataPoints.get(0).getAdjClose());
                                }
                            }).filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(v -> (Double)v)
                            .collect(Collectors.toList());

                    Double average = prices.stream()
                            .reduce(0d, (a, b) -> a + b) / prices.size();
                    return new InitialSectorPrice(sector, average);
                }
            });


    private LoadingCache<LocalDate, Boolean> isTradeDateCache = CacheBuilder.newBuilder()
            .maximumSize(300)
            .build(new CacheLoader<LocalDate, Boolean>() {
                @Override
                public Boolean load(LocalDate key) throws Exception {
                    Query<PriceDataPoint> q = store.find(PriceDataPoint.class);
                    q.criteria("date").lessThan(key.plusDays(1)).and(q.criteria("date").greaterThanOrEq(key));
                    int resSie = q.asList().size();
                    System.out.println(resSie);
                    return resSie > 1500;
                }
            });


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
                                    .map(s -> s.replaceAll("\\s+", "")) //remove whitespace
                                    .collect(Collectors.toList());
                        }
                    }
            );

    public MongoRepo() {
        client = new MongoClient("mongo:27017");
        store = morphia.createDatastore(client, "example");
        morphia.mapPackage("model");
    }

    public MongoRepo(String address) {
        client = new MongoClient(address);
        store = morphia.createDatastore(client, "example");
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
    public List<UserState> getUsersById(List<ObjectId> ids) {
        return store.get(UserState.class, ids).asList();
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
        GameDates startAndEnd = getGameDates();
        LocalDate currentDate = currentDate();
        long startToNow = ChronoUnit.DAYS.between(startAndEnd.getStartDate(),currentDate);
        long nowToEnd = ChronoUnit.DAYS.between(currentDate, startAndEnd.getEndDate());
        long combined = startToNow + nowToEnd;
        double progressInPercent = startToNow / combined  * 100;
        List<UserState> users = getAllUsers().stream()
                .filter(u -> u.getStateComputedAt().isEqual(currentDate))
                .collect(Collectors.toList());
        List<DashboardEntry> entries = users.stream()
                .map(u -> new DashboardEntry(u.getName(), u.getNetWorth()))
                .collect(Collectors.toList());
        return new Dashboard(progressInPercent, entries);
    }

    @Override
    public List<UserState> getLateUsers(LocalDate currentDate) {
        final Query<UserState> query = store.createQuery(UserState.class);
        query.criteria("stateComputedAt").lessThan(currentDate);
        return query.asList();
    }

    @Override
    public PriceHistory getPriceHistory(LocalDate date) {
        if (priceHistory == null) {
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

            priceHistory = new PriceHistory(pointsss);
            return priceHistory;
        } else {
            return priceHistory;
        }

    }

    @Override
    public List<String> getSymbolsForSelector(Selector selector) {

        if (selector instanceof SectorSelector) {
            return mappings.getUnchecked(selector);
        }
        if (selector instanceof SingleCompanySelector) {
            return Lists.newArrayList(((SingleCompanySelector) selector).getSymbol());
        }
        return Lists.newArrayList();
    }

    @Override
    public LocalDate currentDate() {
        try {
            final Query<UserState> query = store.createQuery(UserState.class)
                    .order("-stateComputedAt");
            List<UserState> mostRecentlyUpdatedUser = query.asList(new FindOptions().limit(1));
            if (mostRecentlyUpdatedUser.isEmpty()) {
                return LocalDate.of(2000, 1, 1);
            } else {
                return mostRecentlyUpdatedUser.get(0).getStateComputedAt();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getGameDates().getStartDate();
        }
    }

    @Override
    public GameDates getGameDates() {
        final Query<GameDates> dateQuery = store.createQuery(GameDates.class);
        List<GameDates> dates = dateQuery.asList();
        if (dates.isEmpty())
            return new GameDates(LocalDate.of(2000, 1, 1), LocalDate.of(2010, 1, 1));
        else
            return dates.get(0);
    }

    @Override
    public GameDates setGameDates(GameDates dates) {
        client.getDatabase("example").getCollection("gamedates").drop();
        store.save(dates);
        return dates;
    }

    @Override
    public Boolean isTradingDate(LocalDate date) {
        return isTradeDateCache.getUnchecked(date);
    }

    @Override
    public LocalDate lastTradingDate(LocalDate currentDate) {
        currentDate = currentDate.minusDays(1);
        LocalDate startDate = getGameDates().getStartDate().minusDays(1);
        while (currentDate.isBefore(startDate)) {
            if (isTradeDateCache.getUnchecked(currentDate)) {
                return currentDate;
            }
        }
        return startDate;
    }

    @Override
    public List<CompanyTuple> getAllCompanies() {
        Query<SectorInfo> query = store.createQuery(SectorInfo.class);
        List<SectorInfo> sectors = query.asList();
        return sectors.stream()
                .map(companyRecord -> new CompanyTuple(companyRecord.getSymbol(), companyRecord.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanyTuple> getCompanyTupleBySector(String sector) {
        Query<SectorInfo> query = store.createQuery(SectorInfo.class);
        query.criteria("sector").containsIgnoreCase(sector);
        List<SectorInfo> sectors = query.asList();
        return sectors.stream()
                .map(companyRecord -> new CompanyTuple(companyRecord.getSymbol(), companyRecord.getSector()))
                .collect(Collectors.toList());
    }

    @Override
    public InitialCompanyPrice getInitialPriceForCompany(String symbol) {
        return initialCompanyPriceCache.getUnchecked(symbol);
    }

    @Override
    public InitialSectorPrice getInitialPriceForSector(String sector) {
        return sectorPriceCache.getUnchecked(sector);
    }

    @Override
    public Boolean isUsernameTaken(String username) {
        Query<UserState> query = store.createQuery(UserState.class);
        query.field("name").containsIgnoreCase(username);
        return query.asList().size() > 0;
    }

    @Override
    public Boolean isEmailUsed(String email) {
        Query<UserState> query = store.createQuery(UserState.class);
        query.field("email").containsIgnoreCase(email);
        return query.asList().size() > 0;
    }

    @Override
    public NetworthRecord getHighestNetworth(LocalDate date) {
        List<NetworthRecord> highValue = store.createQuery(NetworthRecord.class).asList();
        if(highValue.isEmpty())
            return new NetworthRecord(date, Double.MAX_VALUE);
        else
            return highValue.stream()
                    .filter(v -> v.getDate().isEqual(date))
                    .findFirst().orElse(new NetworthRecord(date, Double.MAX_VALUE));
    }

    @Override
    public NetworthRecord setHighestNetworthForDate(NetworthRecord record) {
        store.save(record);
        return record;
    }
}
