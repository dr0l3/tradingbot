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
    @Property("Sector")
    private String sector;
    @Property("Industry")
    private String industry;


    public String getSymbol() {
        return symbol;
    }

    public String getSector() {
        return sector;
    }

    public String getName() {
        return name;
    }
}
