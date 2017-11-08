package model.dashboard;

public class DashboardEntry {
    private String userName;
    private Double netWorth;

    public DashboardEntry() {
    }

    public DashboardEntry(String userName, Double netWorth) {
        this.userName = userName;
        this.netWorth = netWorth;
    }

    public String getUserName() {
        return userName;
    }

    public Double getNetWorth() {
        return netWorth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardEntry that = (DashboardEntry) o;

        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        return netWorth != null ? netWorth.equals(that.netWorth) : that.netWorth == null;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (netWorth != null ? netWorth.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DashboardEntry{" +
                "userName='" + userName + '\'' +
                ", netWorth=" + netWorth +
                '}';
    }
}
