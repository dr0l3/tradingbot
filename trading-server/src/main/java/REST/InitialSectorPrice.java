package REST;

public class InitialSectorPrice {
    private String sector;
    private Double price;

    public InitialSectorPrice(String sector, Double price) {
        this.sector = sector;
        this.price = price;
    }

    public InitialSectorPrice() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InitialSectorPrice that = (InitialSectorPrice) o;

        if (sector != null ? !sector.equals(that.sector) : that.sector != null) return false;
        return price != null ? price.equals(that.price) : that.price == null;
    }

    @Override
    public int hashCode() {
        int result = sector != null ? sector.hashCode() : 0;
        result = 31 * result + (price != null ? price.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InitialSectorPrice{" +
                "sector='" + sector + '\'' +
                ", price=" + price +
                '}';
    }
}
