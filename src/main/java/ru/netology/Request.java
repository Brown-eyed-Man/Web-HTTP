package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class Request {
    private final String requestMethod;
    private final String requestPath;
    private final String protocolVersion;
    private final List<NameValuePair> queryParams;

    public Request(String requestMethod, String requestPath, String protocolVersion) throws URISyntaxException {
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.protocolVersion = protocolVersion;
        this.queryParams = URLEncodedUtils.parse(new URI(requestPath), StandardCharsets.UTF_8);
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getProtocolVersion() {
        return protocolVersion;
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
