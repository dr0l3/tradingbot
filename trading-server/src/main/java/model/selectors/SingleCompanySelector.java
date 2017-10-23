package model.selectors;

import com.google.common.collect.Lists;
import model.PriceHistory;
import model.Selector;

import java.util.List;

public class SingleCompanySelector implements Selector {
    private String symbol;

    public SingleCompanySelector() {
    }

    public SingleCompanySelector(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public List<String> matchedSymbols(PriceHistory history) {
        return Lists.newArrayList(symbol);
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "SingleCompanySelector{" +
                "symbol='" + symbol + '\'' +
                '}';
    }
}
