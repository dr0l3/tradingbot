import REST.*;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Selector;
import model.Signal;
import model.UserState;
import model.UserStrategy;
import model.dashboard.Dashboard;
import persistence.MongoRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static spark.Spark.*;
import static spark.Spark.get;

public class API {
    public static void main(String[] args) {
        MongoRepo repo = new MongoRepo();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Selector.class, new GsonInterfaceAdapter<Selector>())
                .registerTypeAdapter(Signal.class, new GsonInterfaceAdapter<Signal>())
                .create();


        Executors.newSingleThreadExecutor().submit(() -> warmUpCache(repo));

        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        post("/addUser", (req, res) -> {
            try {
                res.type("application/json");
                UserState userState = gson.fromJson(req.body(), UserState.class);
                if(userState.getName().length() < 2)
                    return gson.toJson(new StandardResponse(300,"Username too short",null));
                if(repo.isUsernameTaken(userState.getName()))
                    return gson.toJson(new StandardResponse(300,"Username taken",null)); // FIXME: 11/8/17
                if(repo.isEmailUsed(userState.getEmail()))
                    return gson.toJson(new StandardResponse(300,"Email taken",null)); // FIXME: 11/8/17
                double stategies = userState.getStrategies().size();
                if(stategies > 5){
                    return gson.toJson(new StandardResponse(300, "Too many strategies", null));
                }
                Double percentage = 100 / stategies;
                int prio = 1;
                for (UserStrategy str : userState.getStrategies()) {
                    str.setPercantage(percentage);
                    str.setPriority(prio);
                    prio++;
                }
                userState.getStrategies().forEach(str -> str.setHoldings(Lists.newArrayList()));
                userState.setCapital(100000d);
                userState.setNetWorth(100000d);
                LocalDate currentDate = repo.currentDate();
                userState.setInsertedAt(currentDate);
                LocalDate startDate = repo.getGameDates().getStartDate();
                userState.setStateComputedAt(startDate.minusDays(1));
                System.out.println(userState.toString());
                UserState returned = repo.addUser(userState);
                return gson.toJson(new StandardResponse(200, "success", gson.toJsonTree(returned)));
            } catch (Exception e){
                e.printStackTrace();
                System.out.println(e);
                return gson.toJson(new StandardResponse(500, "fail", null));
            }
        });

        get("/allUsers", (req,res) -> {
            res.type("application/json");
            List<UserState> users = repo.getAllUsers();
            return gson.toJson(new StandardResponse(200, "success", gson.toJsonTree(users)));
        });

        get("/dashboard", (req, res) -> {
            res.type("application/json");
            Dashboard db = repo.getCurrentStandings();
            return gson.toJson(gson.toJsonTree(db));
        });

        get("/initialPriceCompany/:symbol", (req,res) -> {
            res.type("application/json");
            String company = req.params(":symbol");
            InitialCompanyPrice price = repo.getInitialPriceForCompany(company);
            return gson.toJson(gson.toJsonTree(price));
        });

        get("/initialPriceSector/:sector", (req,res) -> {
            res.type("application/json");
            String sector = req.params(":sector").replaceAll("%20", " ");
            System.out.println(sector);
            InitialSectorPrice price = repo.getInitialPriceForSector(sector);
            System.out.println(price);
            return gson.toJson(gson.toJsonTree(price));
        });

        get("/allCompanies", (req,res) -> {
            res.type("application/json");
            List<CompanyTuple> companies = repo.getAllCompanies();
            return gson.toJson(gson.toJsonTree(companies));
        });

    }

    private static void warmUpCache(MongoRepo repo) {
        repo.getPriceHistory(LocalDate.of(2014,1,1));
        repo.getInitialPriceForSector("Technology");
        repo.getInitialPriceForSector("Health Care");
        repo.getInitialPriceForSector("Consumer Services");
        repo.getInitialPriceForSector("Capital Goods");
        repo.getInitialPriceForSector("Consumer Durables");
        repo.getInitialPriceForSector("n/a");
        repo.getInitialPriceForSector("Miscellaneous");
        repo.getInitialPriceForSector("Consumer Non-Durables");
        repo.getInitialPriceForSector("Public Utilities");
        repo.getInitialPriceForSector("Basic Industries");
        repo.getInitialPriceForSector("Energy");
        repo.getInitialPriceForSector("Transportation");

    }
}
