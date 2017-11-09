import com.google.common.collect.Lists;
import model.Selector;
import model.Signal;
import model.UserState;
import model.UserStrategy;
import model.dashboard.GameDates;
import model.selectors.SectorSelector;
import model.selectors.SingleCompanySelector;
import model.signals.ConstantSignal;
import persistence.MongoRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameServer {
    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {

        MongoRepo repo = new MongoRepo("localhost:32768");
        Tick ticker = new Tick(repo);


        GameDates toUse = new GameDates(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 4));
        repo.setGameDates(toUse);
        GameDates gameDates2 = repo.getGameDates();
        System.out.println("Startdate: " + gameDates2.getStartDate());
        System.out.println("Enddate: " + gameDates2.getEndDate());

        List<LocalDate> gameDates = Lists.newArrayList();

        LocalDate current = gameDates2.getStartDate();
        while (true) {
            gameDates.add(current);

            if (current.plusDays(1).isAfter(gameDates2.getEndDate())) {
                break;
            }
            current = current.plusDays(1);
        }

//        executor.scheduleWithFixedDelay(() -> {
//            LocalDate currentDate = repo.currentDate();
//            ticker.catchup(currentDate);}, 60, 5, TimeUnit.SECONDS);

        repo.addUser(testTechUser("number1"));
//        repo.addUser(testTechUser());
//        repo.addUser(testFinanceUser());
//        repo.addUser(testEnergyUser());
//        repo.addUser(testTransUser());

        ticker.prepare();
        System.out.print("Date used ");
        System.out.println(gameDates.get(0));
        gameDates.forEach(d -> {
            long before = System.nanoTime();
            List<UserState> states = ticker.tick(d);
            long after = System.nanoTime();
            repo.addUser(testTechUser("User" + d.toString()));
            ticker.catchup(d);
            System.out.println("");
//            states.forEach(u -> {
//                System.out.println("User by name of " +u.getName() + " has networth " + u.getNetWorth() + " capital " + u.getCapital());
//                System.out.println("-- Holdings --");
//                u.getStrategies().forEach(str ->
//                {
//                    System.out.println("Number of holdings: " + str.getHoldings().size());
//                });
            long diff = after - before;
            int tosleep = 1000 - (int) (diff / 1000000);
            try {
                Thread.sleep(tosleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        repo.addUser(testUser("hehhehe"));
        ticker.catchup(gameDates.get(gameDates.size()-1));

        List<UserState>users = repo.getAllUsers();
        System.out.println("User        Networth");
        for (UserState user : users) {
            System.out.printf("%s           %f \n",user.getName(), user.getNetWorth());
        }

//        try {
//            System.out.println("Simulation ended, shutting down");
//            executor.awaitTermination(2, TimeUnit.SECONDS);
//            executor.shutdownNow();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            executor.shutdownNow();
//        }
    }

    private static UserState testUser(String name) {
        UserState state = new UserState();
        UserStrategy str1 = new UserStrategy();
        Signal buy = new ConstantSignal(true);
        Signal sell = new ConstantSignal(false);
        Selector se = new SingleCompanySelector("LYG");
        state.setStateComputedAt(LocalDate.of(2000, 1, 1));
        state.setInsertedAt(LocalDate.of(2000, 1, 1));
        str1.setSelector(se);
        str1.setPriority(1);
        str1.setPercantage(100d);
        str1.setBuySignal(buy);
        str1.setSellSignal(sell);
        state.setCapital(100000d);
        state.setNetWorth(100000d);
        state.setName(name);
        state.setStrategies(Lists.newArrayList(str1));
        return state;
    }

    private static UserState testTechUser(String name) {
        UserState state = new UserState();
        UserStrategy str1 = new UserStrategy();
        Signal buy = new ConstantSignal(true);
        Signal sell = new ConstantSignal(false);
        Selector se = new SectorSelector("Technology");
        state.setStateComputedAt(LocalDate.of(2000, 1, 1));
        state.setInsertedAt(LocalDate.of(2000, 1, 1));
        str1.setSelector(se);
        str1.setPriority(1);
        str1.setPercantage(100d);
        str1.setBuySignal(buy);
        str1.setSellSignal(sell);
        state.setCapital(100000d);
        state.setNetWorth(100000d);
        state.setName(name);
        state.setStrategies(Lists.newArrayList(str1));
        return state;
    }

    private static UserState testFinanceUser() {
        UserState state = new UserState();
        UserStrategy str1 = new UserStrategy();
        Signal buy = new ConstantSignal(true);
        Signal sell = new ConstantSignal(false);
        Selector se = new SectorSelector("Finance");
        str1.setSelector(se);
        str1.setPriority(1);
        str1.setPercantage(100d);
        str1.setBuySignal(buy);
        str1.setSellSignal(sell);
        state.setCapital(100000d);
        state.setName("finance");
        state.setStrategies(Lists.newArrayList(str1));
        return state;
    }

    private static UserState testEnergyUser() {
        UserState state = new UserState();
        UserStrategy str1 = new UserStrategy();
        Signal buy = new ConstantSignal(true);
        Signal sell = new ConstantSignal(false);
        Selector se = new SectorSelector("Energy");
        str1.setSelector(se);
        str1.setPriority(1);
        str1.setPercantage(100d);
        str1.setBuySignal(buy);
        str1.setSellSignal(sell);
        state.setCapital(100000d);
        state.setName("energy");
        state.setStrategies(Lists.newArrayList(str1));
        return state;
    }

    private static UserState testTransUser() {
        UserState state = new UserState();
        UserStrategy str1 = new UserStrategy();
        Signal buy = new ConstantSignal(true);
        Signal sell = new ConstantSignal(false);
        Selector se = new SectorSelector("Transportation");
        str1.setSelector(se);
        str1.setPriority(1);
        str1.setPercantage(100d);
        str1.setBuySignal(buy);
        str1.setSellSignal(sell);
        state.setCapital(100000d);
        state.setName("transportation");
        state.setStrategies(Lists.newArrayList(str1));
        return state;
    }
}
