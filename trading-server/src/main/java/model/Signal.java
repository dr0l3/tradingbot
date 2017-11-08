package model;

import java.time.LocalDate;

public interface Signal {
    boolean isActive(PriceHistory priceHistory, String symbol, LocalDate date);
}
