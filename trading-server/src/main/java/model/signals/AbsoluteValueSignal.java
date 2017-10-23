package model.signals;

import model.PriceHistory;
import model.Selector;
import model.Signal;

import java.util.List;

public class AbsoluteValueSignal implements Signal {
    private Double cap;
    private boolean activeAboveCap;
    private Selector selector;

    public AbsoluteValueSignal() {
    }

    public AbsoluteValueSignal(Double cap, boolean upper, Selector selector) {
        this.cap = cap;
        this.activeAboveCap = upper;
        this.selector = selector;
    }

    @Override
    public boolean isActive(PriceHistory priceHistory) {
        List<String> symbols = selector.matchedSymbols(priceHistory);
        return symbols.stream().anyMatch(symbol -> priceHistory.getSalesPriceForSymbol(symbol)
                .map(price -> price > cap == activeAboveCap)
                .orElse(false));
    }

    @Override
    public String toString() {
        return "AbsoluteValueSignal{" +
                "cap=" + cap +
                ", activeAboveCap=" + activeAboveCap +
                ", selector=" + selector +
                '}';
    }
}
