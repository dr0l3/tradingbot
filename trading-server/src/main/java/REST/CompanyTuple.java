package REST;

public class CompanyTuple {
    private String symbol;
    private String name;

    public CompanyTuple() {
    }

    public CompanyTuple(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }
}
