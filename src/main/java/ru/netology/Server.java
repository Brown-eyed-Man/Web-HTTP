package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.netology.handlers.Handler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers;
    private final ExecutorService threadPool;
    private final int limit = 4096;

    public Server(int threadQuantity) {
        handlers = new ConcurrentHashMap<>();
        threadPool = Executors.newFixedThreadPool(threadQuantity);
    }

    public void start(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                final Socket socket = serverSocket.accept();
                threadPool.execute(() -> handleConnection(socket));
            }
        } catch (IOException e) {

        }
        finally {
            threadPool.shutdown();
        }
    }

    public void handleConnection(Socket socket) {
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            List<String> allowedMethods = List.of(HttpMethod.GET, HttpMethod.POST);

            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }

            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                return;
            }

            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
                return;
            }

            final var path = requestLine[1];
            if (!path.startsWith("/") || !validPaths.contains(path)) {
                badRequest(out);
                return;
            }

            final var headersDelimiter = new byte[] {'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }

            in.reset();

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            List<String> headers = List.of(new String(headersBytes).split("\r\n"));
            System.out.println(headers);

            String body = null;
            if (!method.equals(HttpMethod.GET)) {
                in.skip(headersDelimiter.length);
                Optional<String> contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    int length = Integer.parseInt(contentLength.get());
                    byte[] bodyBytes = in.readNBytes(length);
                    body = new String(bodyBytes);
                    System.out.println(body);
                }
            }
//            System.out.println(Arrays.toString(requestLine));

            URI uriPath = new URI(path);
            List<NameValuePair> params = URLEncodedUtils.parse(uriPath, StandardCharsets.UTF_8);
            String mimeType = Files.probeContentType(Path.of(".", "public", uriPath.getPath()));

            Request request = new Request(
                    method,
                    uriPath.getPath(),
                    headers,
                    uriPath.getQuery(),
                    mimeType,
                    body,
                    params);

            if (!handlers.containsKey(request.getMethod())) {
                notFound(out);
                return;
            }

            var methodHandlerMap = handlers.get(request.getMethod());

            if (!methodHandlerMap.containsKey(request.getPath())) {
                notFound(out);
                return;
            }

            var handler = methodHandlerMap.get(request.getPath());
            handler.handle(request, out);

        } catch (NullPointerException e) {
            e.getMessage();
        } catch (IOException | URISyntaxException e) {

        }
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    protected void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    protected void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public void addHandler(String requestMethod, String path, Handler handler) {
        if (!handlers.containsKey(requestMethod)) {
            handlers.put(requestMethod, new ConcurrentHashMap<>());
        }
        handlers.get(requestMethod).put(path, handler);
    }
}