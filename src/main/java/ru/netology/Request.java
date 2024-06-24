package ru.netology;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Optional;

public class Request {
    private final String method;
    private final String path;
    private final List<String> headers;
    private final String queryString;
    private final String contentType;
    private final String body;
    private final List<NameValuePair> queryParams;

    public Request(String method, String path, List<String> headers, String queryString, String contentType, String body, List<NameValuePair> queryParams) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryString = queryString;
        this.contentType = contentType;
        this.body = body;
        this.queryParams = queryParams;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public Optional<String> getQueryParam(String name) {
        return queryParams.stream()
                .filter(o -> o.getName().equals(name))
                .findFirst()
                .map(NameValuePair::getValue);
    }
}