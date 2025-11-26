package academy.model;

public class Resource {
    private String resource;
    private Integer totalRequestsCount;

    public Resource(String resource, Integer totalRequestsCount) {
        this.resource = resource;
        this.totalRequestsCount = totalRequestsCount;
    }

    public Resource() {}

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getTotalRequestsCount() {
        return totalRequestsCount;
    }

    public void setTotalRequestsCount(Integer totalRequestsCount) {
        this.totalRequestsCount = totalRequestsCount;
    }
}
