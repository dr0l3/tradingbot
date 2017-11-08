import REST.GsonInterfaceAdapter;
import REST.StandardResponse;
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

import static spark.Spark.*;
import static spark.Spark.get;

public class API {
    public static void main(String[] args) {
        MongoRepo repo = new MongoRepo();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Selector.class, new GsonInterfaceAdapter<Selector>())
                .registerTypeAdapter(Signal.class, new GsonInterfaceAdapter<Signal>())
                .create();

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
                double stategies = userState.getStrategies().size();
                Double percentage = 100 / stategies;
                int prio = 1;
                for (UserStrategy str : userState.getStrategies()) {
                    str.setPercantage(percentage);
                    str.setPriority(prio);
                    prio++;
                }
                LocalDate currentDate = repo.currentDate();
                userState.setInsertedAt(currentDate);
                userState.setStateComputedAt(currentDate);
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

    }
}
