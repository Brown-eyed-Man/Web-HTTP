package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Server {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers;

    public Server() {
        handlers = new ConcurrentHashMap<>();
    }

    public void start(int port, int threadNumbers) {
        try (final var serverSocket = new ServerSocket(port);
             final var executorService = Executors.newFixedThreadPool(threadNumbers)) {
            while (!serverSocket.isClosed()) {
                final Socket socket = serverSocket.accept();
                executorService.execute(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleConnection(Socket socket) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                socket.close();
                return;
            }

            final var method = parts[0];
            final var path = parts[1];
            final var protocolVersion = parts[2];
            System.out.println(method+"\n"+path+"\n"+protocolVersion);
            Request request = new Request(method, path, protocolVersion);

            if (!handlers.containsKey(request.getRequestMethod())) {
                notFound(out);
                return;
            }

            handlers.get(request.getRequestMethod()).get(request.getRequestPath()).handle(request, out);
            System.out.println(request.getQueryParams());

        } catch (NullPointerException e) {
            e.getMessage();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
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

    public void addHandler(String requestMethod, String path, Handler handler) {
        if (!handlers.containsKey(requestMethod)) {
            handlers.put(requestMethod, new ConcurrentHashMap<>());
        }
        handlers.get(requestMethod).put(path, handler);
    }

    public static List<String> getValidPaths() {
        return validPaths;
    }
}