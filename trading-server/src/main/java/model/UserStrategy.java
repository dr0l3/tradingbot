package model;

import com.google.common.collect.Lists;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import persistence.Repo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

//@Converters({SignalConverter.class, SelectorConverter.class})
public class UserStrategy {
    @Id
    private ObjectId id = new ObjectId();
    private Signal buySignal;
    private Signal sellSignal;
    private Selector selector; // TODO: 10/12/17 What to do?
    private Integer priority;
    private Double percantage; //should be a number between 0 and 100
    private List<Holding> holdings = Lists.newArrayList();

    public UserStrategy() {
    }

    public UserStrategy(Signal buySignal, Signal sellSignal, Selector selector, Integer priority, Double percantage, List<Holding> holdings) {
        this.buySignal = buySignal;
        this.sellSignal = sellSignal;
        this.selector = selector;
        this.priority = priority;
        this.percantage = percantage;
        this.holdings = holdings;
    }

    public UserStrategy(UserStrategy another){
        this.buySignal = another.getBuySignal();
        this.sellSignal = another.getSellSignal();
        this.priority = another.getPriority();
        this.selector = another.getSelector();
        this.percantage = another.getPercantage();
        this.holdings = another.getHoldings();
    }

    public boolean isActive(PriceHistory priceHistory, Repo repo, LocalDate date){
        List<String> symbols = selector.matchedSymbols(priceHistory, repo);
        return symbols.stream()
                .anyMatch(symbol -> buySignal.isActive(priceHistory, symbol, date) && !sellSignal.isActive(priceHistory, symbol, date));
    }

    public List<String> activeSymbols(PriceHistory priceHistory, Repo repo, LocalDate date){
        return selector.matchedSymbols(priceHistory, repo).stream()
                .filter(symbol -> buySignal.isActive(priceHistory,symbol, date) && ! sellSignal.isActive(priceHistory,symbol, date))
                .collect(Collectors.toList());
    }


    public Signal getBuySignal() {
        return buySignal;
    }

    public Signal getSellSignal() {
        return sellSignal;
    }

    public int getPriority() {
        return priority;
    }

    public double getPercantage() {
        return percantage;
    }

    public List<Holding> getHoldings() {
        return holdings;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setPercantage(Double percantage) {
        this.percantage = percantage;
    }

    public void setBuySignal(Signal buySignal) {
        this.buySignal = buySignal;
    }

    public void setSellSignal(Signal sellSignal) {
        this.sellSignal = sellSignal;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setPercantage(double percantage) {
        this.percantage = percantage;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }

    @Override
    public String toString() {
        return "UserStrategy{" +
                "id=" + id +
                ", buySignal=" + buySignal +
                ", sellSignal=" + sellSignal +
                ", selector=" + selector +
                ", priority=" + priority +
                ", percantage=" + percantage +
                ", holdings=" + holdings +
                '}';
    }
}
