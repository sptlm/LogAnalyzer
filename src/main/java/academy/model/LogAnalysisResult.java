package academy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogAnalysisResult {

    private List<String> files;
    private int totalRequestsCount;
    private Map<String, Double> responseSizeInBytes;
    private List<Map<String, Object>> resources;
    private List<Map<String, Object>> responseCodes;
    private List<Map<String, Object>> requestsPerDate;
    private List<String> uniqueProtocols;

    public LogAnalysisResult() {
        this.files = new ArrayList<>();
        this.responseSizeInBytes = new HashMap<>();
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

    public Map<String, Double> getResponseSizeInBytes() {
        return responseSizeInBytes;
    }

    public void setResponseSizeInBytes(Map<String, Double> responseSizeInBytes) {
        this.responseSizeInBytes = responseSizeInBytes;
    }

    public List<Map<String, Object>> getResources() {
        return resources;
    }

    public void setResources(List<Map<String, Object>> resources) {
        this.resources = resources;
    }

    public List<Map<String, Object>> getResponseCodes() {
        return responseCodes;
    }

    public void setResponseCodes(List<Map<String, Object>> responseCodes) {
        this.responseCodes = responseCodes;
    }

    public List<Map<String, Object>> getRequestsPerDate() {
        return requestsPerDate;
    }

    public void setRequestsPerDate(List<Map<String, Object>> requestsPerDate) {
        this.requestsPerDate = requestsPerDate;
    }

    public List<String> getUniqueProtocols() {
        return uniqueProtocols;
    }

    public void setUniqueProtocols(List<String> uniqueProtocols) {
        this.uniqueProtocols = uniqueProtocols;
    }
}
