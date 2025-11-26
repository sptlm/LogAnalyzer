package academy.model;

import java.time.LocalDateTime;

public class Log {
    private String remoteAddr; // IP адрес клиента
    private String timeLocal; // Время запроса
    private String request; // Полная строка запроса (METHOD /resource HTTP/VERSION)
    private int status; // HTTP код ответа
    private long bodyBytes; // Размер ответа в байтах
    private String resource; // Ресурс (извлечено из request)
    private String protocol; // Протокол (извлечено из request)
    private LocalDateTime date; // Дата запроса

    public Log() {}

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getTimeLocal() {
        return timeLocal;
    }

    public void setTimeLocal(String timeLocal) {
        this.timeLocal = timeLocal;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getBodyBytes() {
        return bodyBytes;
    }

    public void setBodyBytes(long bodyBytes) {
        this.bodyBytes = bodyBytes;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
