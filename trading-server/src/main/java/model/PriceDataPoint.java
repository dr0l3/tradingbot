package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.time.LocalDate;

@Entity("prices")
public class PriceDataPoint {
    @Id
    private ObjectId id;
    private String symbol;
    private LocalDate date;
//    private Double open;
//    private Double high;
//    private Double low;
//    private Double close;
    private Double adjClose;

    public ObjectId getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public LocalDate getDate() {
        return date;
    }

    public Double getAdjClose() {
        return adjClose;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PriceDataPoint that = (PriceDataPoint) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        return adjClose != null ? adjClose.equals(that.adjClose) : that.adjClose == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (adjClose != null ? adjClose.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PriceDataPoint{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", date=" + date +
                ", adjClose=" + adjClose +
                '}';
    }
}
