package academy.model;

public class ResponseSizeStats {

    private Double average;
    private Double max;
    private Double p95;

    public ResponseSizeStats(Double average, Double max, Double p95) {
        this.average = average;
        this.max = max;
        this.p95 = p95;
    }

    public ResponseSizeStats() {}

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getP95() {
        return p95;
    }

    public void setP95(Double p95) {
        this.p95 = p95;
    }
}
