package model.signals;

import model.PriceHistory;
import model.Signal;

import java.time.LocalDate;

public class PriceSignal implements Signal {
    private Double cap;
    private boolean activeAboveCap;

    public PriceSignal() {
    }

    public PriceSignal(Double cap, boolean upper) {
        this.cap = cap;
        this.activeAboveCap = upper;
    }

    @Override
    public String toString() {
        return "PriceSignal{" +
                "cap=" + cap +
                ", activeAboveCap=" + activeAboveCap +
                '}';
    }

    @Override
    public boolean isActive(PriceHistory priceHistory, String symbol, LocalDate date) {
        return priceHistory.getClosePriceForSymbol(symbol, date)
                .map(price -> price > cap == activeAboveCap)
                .orElse(false);
    }
}
