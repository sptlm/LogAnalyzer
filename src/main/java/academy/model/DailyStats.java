package academy.model;

public class DailyStats {
    private String date;
    private String weekday;
    private Integer totalRequestsCount;
    private Double totalRequestsPercentage;

    public DailyStats(String date, String weekday, Integer totalRequestsCount, Double totalRequestsPercentage) {
        this.date = date;
        this.weekday = weekday;
        this.totalRequestsCount = totalRequestsCount;
        this.totalRequestsPercentage = totalRequestsPercentage;
    }

    public DailyStats() {}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public Integer getTotalRequestsCount() {
        return totalRequestsCount;
    }

    public void setTotalRequestsCount(Integer totalRequestsCount) {
        this.totalRequestsCount = totalRequestsCount;
    }

    public Double getTotalRequestsPercentage() {
        return totalRequestsPercentage;
    }

    public void setTotalRequestsPercentage(Double totalRequestsPercentage) {
        this.totalRequestsPercentage = totalRequestsPercentage;
    }
}
