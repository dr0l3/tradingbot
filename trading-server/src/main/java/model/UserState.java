package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.util.List;

@Entity("userstate")
public class UserState {
    @Id private ObjectId id;
    private List<UserStrategy> strategies;
    private Double capital;
    private String name;

    public UserState() {
    }

    public UserState(List<UserStrategy> strategies, Double capital, String name) {
        this.strategies = strategies;
        this.capital = capital;
        this.name = name;
    }

    public UserState(ObjectId id, List<UserStrategy> strategies, Double capital, String name) {
        this.id = id;
        this.strategies = strategies;
        this.capital = capital;
        this.name = name;
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

    @Override
    public String toString() {
        return "UserState{" +
                "id=" + id +
                ", strategies=" + strategies +
                ", capital=" + capital +
                '}';
    }
}
