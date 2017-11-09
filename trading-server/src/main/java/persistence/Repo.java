package persistence;

import REST.CompanyTuple;
import REST.InitialSectorPrice;
import model.NetworthRecord;
import model.PriceHistory;
import model.Selector;
import model.UserState;
import model.dashboard.Dashboard;
import model.dashboard.GameDates;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

public interface Repo {
    UserState addUser(UserState userState);
    List<UserState> getAllUsers();
    List<UserState> getUsersById(List<ObjectId> ids);
    UserState updateUser(UserState userState);
    List<UserState> updateUsers(List<UserState> users);
    Dashboard getCurrentStandings();
    List<UserState> getLateUsers(LocalDate currentDate);
    PriceHistory getPriceHistory(LocalDate date);
    List<String> getSymbolsForSelector(Selector selector);
    LocalDate currentDate();
    GameDates getGameDates();
    GameDates setGameDates(GameDates dates);
    Boolean isTradingDate(LocalDate date);
    LocalDate lastTradingDate(LocalDate currentDate);
    List<CompanyTuple> getAllCompanies();
    List<CompanyTuple> getCompanyTupleBySector(String sector);
    REST.InitialCompanyPrice getInitialPriceForCompany(String symbol);
    InitialSectorPrice getInitialPriceForSector(String sector);
    Boolean isUsernameTaken(String username);
    Boolean isEmailUsed(String email);
    NetworthRecord getHighestNetworth(LocalDate date);
    NetworthRecord setHighestNetworthForDate(NetworthRecord record);
}
