package model;


import persistence.Repo;

import java.util.List;

public interface Selector {
    List<String> matchedSymbols(PriceHistory history, Repo repo);
}
