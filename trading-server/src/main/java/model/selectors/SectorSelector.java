package model.selectors;

import model.PriceHistory;
import model.Selector;
import persistence.Repo;

import java.util.List;

public class SectorSelector implements Selector {
    private String sectorName;

    public SectorSelector() {
    }

    public SectorSelector(String sectorName) {
        this.sectorName = sectorName;
    }

    @Override
    public List<String> matchedSymbols(PriceHistory history, Repo repo) {
        return repo.getSymbolsForSelector(this);
    }

    public String getSectorName() {
        return sectorName;
    }

    @Override
    public String toString() {
        return "SectorSelector{" +
                "sectorName='" + sectorName + '\'' +
                '}';
    }
}
