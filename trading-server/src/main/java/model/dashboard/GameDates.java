package model.dashboard;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.time.LocalDate;

@Entity("gamedates")
public class GameDates {
    @Id private ObjectId id;
    private LocalDate startDate;
    private LocalDate endDate;

    public GameDates(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public GameDates() {
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameDates gameDates = (GameDates) o;

        if (startDate != null ? !startDate.equals(gameDates.startDate) : gameDates.startDate != null) return false;
        return endDate != null ? endDate.equals(gameDates.endDate) : gameDates.endDate == null;
    }

    @Override
    public int hashCode() {
        int result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GameDates{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
