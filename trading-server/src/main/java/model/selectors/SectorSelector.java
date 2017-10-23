package model.selectors;

import model.PriceHistory;
import model.Selector;

import java.util.List;

public class SectorSelector implements Selector {
    private String sectorName;

    public SectorSelector() {
    }

    public SectorSelector(String sectorName) {
        this.sectorName = sectorName;
    }

    @Override
    public List<String> matchedSymbols(PriceHistory history) {
        return history.getSymbolsForSelector(this);
    }

    public String getSectorName() {
        return sectorName;
    }
}
