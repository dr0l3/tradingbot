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


        GameDates toUse = new GameDates(LocalDate.of(2014,1,1), LocalDate.of(2014,12,31));
        repo.setGameDates(toUse);
        GameDates gameDates2 = repo.getGameDates();
        System.out.println("Startdate: " + gameDates2.getStartDate());
        System.out.println("Enddate: " + gameDates2.getEndDate());

        List<LocalDate> gameDates = Lists.newArrayList();

        LocalDate current = gameDates2.getStartDate();
        while(true){
            gameDates.add(current);
            if(current.plusDays(1).isAfter(gameDates2.getEndDate())){
                break;
            }
            current = current.plusDays(1);
        }

        executor.scheduleWithFixedDelay(() -> {
            LocalDate currentDate = repo.currentDate();
            ticker.catchup(currentDate);}, 60, 5, TimeUnit.SECONDS);

        repo.addUser(testUser());
        repo.addUser(testTechUser());
        repo.addUser(testFinanceUser());
        repo.addUser(testEnergyUser());
        repo.addUser(testTransUser());

        ticker.prepare();
        gameDates.forEach(d -> {
            long before = System.nanoTime();
            List<UserState> states = ticker.tick(d);
            long after = System.nanoTime();
            System.out.println("");
            states.forEach(u -> {
                System.out.println("User by name of " +u.getName() + " has networth " + u.getNetWorth() + " capital " + u.getCapital());
                System.out.println("-- Holdings --");
                u.getStrategies().forEach(str ->
                {
                    System.out.println("Number of holdings: " + str.getHoldings().size());
                });
            });
            long diff = after-before;
            int tosleep = 1000 - (int) (diff/1000000);
//            try {
//                Thread.sleep(tosleep);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        });


        try {
            System.out.println("Simulation ended, shutting down");
            executor.awaitTermination(2, TimeUnit.SECONDS);
            executor.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static UserState testUser(){
        UserState state = new UserState();
        UserStrategy str1 = new UserStrategy();
        Signal buy = new ConstantSignal(true);
        Signal sell = new ConstantSignal(false);
        Selector se = new SingleCompanySelector("LYG");
        str1.setSelector(se);
        str1.setPriority(1);
        str1.setPercantage(100d);
        str1.setBuySignal(buy);
        str1.setSellSignal(sell);
        state.setCapital(100000d);
        state.setName("test1");
        state.setStrategies(Lists.newArrayList(str1));
        return state;
    }

    private static UserState testTechUser(){
        UserState state = new UserState();
        UserStrategy str1 = new UserStrategy();
        Signal buy = new ConstantSignal(true);
        Signal sell = new ConstantSignal(false);
        Selector se = new SectorSelector("Technology");
        str1.setSelector(se);
        str1.setPriority(1);
        str1.setPercantage(100d);
        str1.setBuySignal(buy);
        str1.setSellSignal(sell);
        state.setCapital(100000d);
        state.setName("tech");
        state.setStrategies(Lists.newArrayList(str1));
        return state;
    }

    private static UserState testFinanceUser(){
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

    private static UserState testEnergyUser(){
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

    private static UserState testTransUser(){
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
