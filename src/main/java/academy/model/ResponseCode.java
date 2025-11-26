package academy.model;

public class ResponseCode {
    private Integer code;
    private Integer totalResponsesCount;

    public ResponseCode(Integer code, Integer totalResponsesCount) {
        this.code = code;
        this.totalResponsesCount = totalResponsesCount;
    }

    public ResponseCode() {}

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getTotalResponsesCount() {
        return totalResponsesCount;
    }

    public void setTotalResponsesCount(Integer totalResponsesCount) {
        this.totalResponsesCount = totalResponsesCount;
    }
}
