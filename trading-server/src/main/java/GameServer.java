import com.google.common.collect.Lists;
import model.*;
import model.dashboard.GameDates;
import model.selectors.SectorSelector;
import model.selectors.SingleCompanySelector;
import model.signals.ConstantSignal;
import persistence.MongoRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameServer {
    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final int MIN_TICK_TIME_IN_MS = 1000;
    private static final int MIN_TICK_TIME_IN_MS_PROD = (60 * 1000);

    public static void main(String[] args) {
//        MongoRepo repo = new MongoRepo("localhost:32768");
//        Tick ticker = new Tick(repo);
        run(args);
    }

    public static void run(String[] args) {

        MongoRepo repo = new MongoRepo();
        Tick ticker = new Tick(repo);


        GameDates toUse = new GameDates(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 12, 31));
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

        ticker.prepare();
        executor.scheduleWithFixedDelay(() -> {
            LocalDate currentDate = repo.currentDate();
            ticker.catchup(currentDate);

        },1,120,TimeUnit.SECONDS);

        gameDates.forEach(d -> {
            long before = System.nanoTime();
            ticker.tick(d);
            long after = System.nanoTime();
            long diff = after - before;
            int tosleep = Math.max(0,MIN_TICK_TIME_IN_MS - (int) (diff / 1000000));
            try {
                Thread.sleep(tosleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        try {
            System.out.println("Simulation ended, shutting down");
            executor.awaitTermination(2, TimeUnit.SECONDS);
            executor.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
            executor.shutdownNow();
        }

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
