package model;


import java.util.List;

public interface Selector {
    List<String> matchedSymbols(PriceHistory history);
}
