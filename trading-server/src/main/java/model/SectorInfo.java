package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity("sectorinfo")
public class SectorInfo {
    @Id private ObjectId id;
    @Property("Symbol")
    private String symbol;
    @Property("Name")
    private String name;
    @Property("LastSale")
    private Double lastSale;
    @Property("MarketCap")
    private Double marketCap;
    @Property("IPOyear")
    private String ipoYear;
    @Property("Sector")
    private String sector;
    @Property("Industry")
    private String industry;
    @Property("Summary Quote")
    private String summaryQuote;


    public String getSymbol() {
        return symbol;
    }
}
