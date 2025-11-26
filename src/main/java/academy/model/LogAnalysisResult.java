package academy.model;

import java.util.ArrayList;
import java.util.List;

public class LogAnalysisResult {

    private List<String> files;
    private int totalRequestsCount;
    private ResponseSizeStats responseSizeInBytes;
    private List<Resource> resources;
    private List<ResponseCode> responseCodes;
    private List<DailyStats> requestsPerDate;
    private List<String> uniqueProtocols;

    public LogAnalysisResult() {
        this.files = new ArrayList<>();
        this.responseSizeInBytes = new ResponseSizeStats();
        this.resources = new ArrayList<>();
        this.responseCodes = new ArrayList<>();
        this.requestsPerDate = new ArrayList<>();
        this.uniqueProtocols = new ArrayList<>();
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public int getTotalRequestsCount() {
        return totalRequestsCount;
    }

    public void setTotalRequestsCount(int totalRequestsCount) {
        this.totalRequestsCount = totalRequestsCount;
    }

    public ResponseSizeStats getResponseSizeInBytes() {
        return responseSizeInBytes;
    }

    public void setResponseSizeInBytes(ResponseSizeStats responseSizeInBytes) {
        this.responseSizeInBytes = responseSizeInBytes;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<ResponseCode> getResponseCodes() {
        return responseCodes;
    }

    public void setResponseCodes(List<ResponseCode> responseCodes) {
        this.responseCodes = responseCodes;
    }

    public List<DailyStats> getRequestsPerDate() {
        return requestsPerDate;
    }

    public void setRequestsPerDate(List<DailyStats> requestsPerDate) {
        this.requestsPerDate = requestsPerDate;
    }

    public List<String> getUniqueProtocols() {
        return uniqueProtocols;
    }

    public void setUniqueProtocols(List<String> uniqueProtocols) {
        this.uniqueProtocols = uniqueProtocols;
    }
}
