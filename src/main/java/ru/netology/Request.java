package ru.netology;

public class Request {
    private final String requestMethod;
    private final String requestPath;

    public Request(String requestMethod, String requestHeaders) {
        this.requestMethod = requestMethod;
        this.requestPath = requestHeaders;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }
}
