package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.time.LocalDate;

@Entity("holding")
public class Holding {
    @Id
    private ObjectId id = new ObjectId();
    private String symbol;
    private Double volume;

    public Holding() {
    }

    public Holding(String symbol, Double volume) {
        this.symbol = symbol;
        this.volume = volume;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "Holding{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", volume=" + volume +
                '}';
    }
}
