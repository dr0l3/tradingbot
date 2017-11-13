package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.time.LocalDate;
import java.util.List;

@Entity("userstate")
public class UserState {
    @Id private ObjectId id;
    private List<UserStrategy> strategies;
    private Double capital = 100000d;
    private String email;
    private String name;
    private Double netWorth;
    private LocalDate stateComputedAt;
    private LocalDate insertedAt;

    public UserState() {
    }

    public UserState(ObjectId id, List<UserStrategy> strategies, Double capital, String email, String name, Double netWorth, LocalDate stateComputedAt, LocalDate insertedAt) {
        this.id = id;
        this.strategies = strategies;
        this.capital = capital;
        this.email = email;
        this.name = name;
        this.netWorth = netWorth;
        this.stateComputedAt = stateComputedAt;
        this.insertedAt = insertedAt;
    }

    public void setNetWorth(Double netWorth) {
        this.netWorth = netWorth;
    }

    public LocalDate getStateComputedAt() {
        return stateComputedAt;
    }

    public void setStateComputedAt(LocalDate stateComputedAt) {
        this.stateComputedAt = stateComputedAt;
    }

    public LocalDate getInsertedAt() {
        return insertedAt;
    }

    public void setInsertedAt(LocalDate insertedAt) {
        this.insertedAt = insertedAt;
    }

    public Double getNetWorth() {
        return netWorth;
    }

    public List<UserStrategy> getStrategies() {
        return strategies;
    }

    public double getCapital() {
        return capital;
    }

    public String getName() {
        return name;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setCapital(Double capital) {
        this.capital = capital;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCapital(double capital) {
        this.capital = capital;
    }

    public void setStrategies(List<UserStrategy> strategies) {
        this.strategies = strategies;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserState userState = (UserState) o;

        if (id != null ? !id.equals(userState.id) : userState.id != null) return false;
        if (strategies != null ? !strategies.equals(userState.strategies) : userState.strategies != null) return false;
        if (capital != null ? !capital.equals(userState.capital) : userState.capital != null) return false;
        if (email != null ? !email.equals(userState.email) : userState.email != null) return false;
        if (name != null ? !name.equals(userState.name) : userState.name != null) return false;
        if (netWorth != null ? !netWorth.equals(userState.netWorth) : userState.netWorth != null) return false;
        if (stateComputedAt != null ? !stateComputedAt.equals(userState.stateComputedAt) : userState.stateComputedAt != null)
            return false;
        return insertedAt != null ? insertedAt.equals(userState.insertedAt) : userState.insertedAt == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (strategies != null ? strategies.hashCode() : 0);
        result = 31 * result + (capital != null ? capital.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (netWorth != null ? netWorth.hashCode() : 0);
        result = 31 * result + (stateComputedAt != null ? stateComputedAt.hashCode() : 0);
        result = 31 * result + (insertedAt != null ? insertedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserState{" +
                "id=" + id +
                ", strategies=" + strategies +
                ", capital=" + capital +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", netWorth=" + netWorth +
                ", stateComputedAt=" + stateComputedAt +
                ", insertedAt=" + insertedAt +
                '}';
    }
}
