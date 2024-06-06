package ru.netology;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    private static final int PORT = 9999;
    private static final int THREADS_QUANTITY = 64;

    public static void main(String[] args) {
        Server server = new Server();

        for (int i = 0; i < Server.getValidPaths().size(); i++) {
            String path = Server.getValidPaths().get(i);
            server.addHandler("GET", path, ((request, responseStream) -> {
                try {
                    final var filePath = Path.of(".", "public", request.getRequestPath());
                    final var mimeType = Files.probeContentType(filePath);

                    // special case for classic
                    if (filePath.equals("/classic.html")) {
                        final var template = Files.readString(filePath);
                        final var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        responseStream.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        responseStream.write(content);
                        responseStream.flush();
                        return;
                    }

                    final var length = Files.size(filePath);
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, responseStream);
                    responseStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        server.start(PORT, THREADS_QUANTITY);
    }
}