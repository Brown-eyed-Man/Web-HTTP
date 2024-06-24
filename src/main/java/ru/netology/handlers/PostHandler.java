package ru.netology.handlers;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.netology.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PostHandler implements Handler {
    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException, URISyntaxException {
        String template = Files.readString(Path.of(".", "public", request.getPath()));
        if (!request.getQueryParams().isEmpty()) {
            StringBuilder params = new StringBuilder();
            for (NameValuePair param : request.getQueryParams()) {
                params.append("Param name: ")
                        .append(param.getName())
                        .append(", Param Value: ")
                        .append(param.getValue());
            }
            template = template.concat(params.toString());
        }

        if (!request.getBody().isEmpty()) {
            StringBuilder params = new StringBuilder();
            List<NameValuePair> bodyParams = URLEncodedUtils.parse(new URI(request.getPath() + "?" + request.getBody()), StandardCharsets.UTF_8);
            for (NameValuePair param : bodyParams) {
                params.append("Param name: ")
                        .append(param.getName())
                        .append(", Param value: ")
                        .append(param.getValue());
            }
            template = template.concat(params.toString());
        }

        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + request.getContentType() + "\r\n" +
                        "Content-Length: " + template.getBytes().length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
                ).getBytes());
        responseStream.write(template.getBytes());
        responseStream.flush();
    }
}