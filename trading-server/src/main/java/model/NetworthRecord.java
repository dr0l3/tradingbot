package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

import java.time.LocalDate;

public class NetworthRecord {
    @Id private ObjectId id;
    private LocalDate date;
    private Double networth;

    public NetworthRecord(LocalDate date, Double networth) {
        this.date = date;
        this.networth = networth;
    }

    public NetworthRecord() {
    }

    public ObjectId getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Double getNetworth() {
        return networth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworthRecord that = (NetworthRecord) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        return networth != null ? networth.equals(that.networth) : that.networth == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (networth != null ? networth.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NetworthRecord{" +
                "id=" + id +
                ", date=" + date +
                ", networth=" + networth +
                '}';
    }
}
