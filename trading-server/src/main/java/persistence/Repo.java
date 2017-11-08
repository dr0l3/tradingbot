package persistence;

import model.PriceHistory;
import model.Selector;
import model.UserState;
import model.dashboard.Dashboard;
import model.dashboard.GameDates;

import java.time.LocalDate;
import java.util.List;

public interface Repo {
    UserState addUser(UserState userState);
    List<UserState> getAllUsers();
    UserState updateUser(UserState userState);
    List<UserState> updateUsers(List<UserState> users);
    Dashboard getCurrentStandings();
    List<UserState> getLateUsers(LocalDate currentDate);
    PriceHistory getPriceHistory(LocalDate date);
    List<String> getSymbolsForSelector(Selector selector);
    LocalDate currentDate();
    GameDates getGameDates();
    GameDates setGameDates(GameDates dates);
}
