package model.dashboard;

import java.util.List;

public class Dashboard {
    private Double progressInPercent; //from 0 to 100;
    private List<DashboardEntry> entries;

    public Dashboard() {
    }

    public Dashboard(Double progressInPercent, List<DashboardEntry> entries) {
        this.progressInPercent = progressInPercent;
        this.entries = entries;
    }

    public Double getProgressInPercent() {
        return progressInPercent;
    }

    public List<DashboardEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dashboard dashboard = (Dashboard) o;

        if (progressInPercent != null ? !progressInPercent.equals(dashboard.progressInPercent) : dashboard.progressInPercent != null)
            return false;
        return entries != null ? entries.equals(dashboard.entries) : dashboard.entries == null;
    }

    @Override
    public int hashCode() {
        int result = progressInPercent != null ? progressInPercent.hashCode() : 0;
        result = 31 * result + (entries != null ? entries.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Dashboard{" +
                "progressInPercent=" + progressInPercent +
                ", entries=" + entries +
                '}';
    }
}
