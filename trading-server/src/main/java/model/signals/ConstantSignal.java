package model.signals;

import model.PriceHistory;
import model.Selector;
import model.Signal;

import java.time.LocalDate;

public class ConstantSignal implements Signal {
    private boolean isAlwaysActive;


    public ConstantSignal(boolean isAlwaysActive) {
        this.isAlwaysActive = isAlwaysActive;
    }

    public ConstantSignal() {
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstantSignal that = (ConstantSignal) o;

        return isAlwaysActive == that.isAlwaysActive;
    }

    @Override
    public int hashCode() {
        return (isAlwaysActive ? 1 : 0);
    }

    @Override
    public String toString() {
        return "ConstantSignal{" +
                "isAlwaysActive=" + isAlwaysActive +
                '}';
    }

    @Override
    public boolean isActive(PriceHistory priceHistory, String symbol, LocalDate date) {
        return isAlwaysActive;
    }
}
