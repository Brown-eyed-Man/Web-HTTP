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
            server.addHandler("GET", path, new RequestHandler());
            server.addHandler("POST", path, new RequestHandler());
        }

        server.start(PORT, THREADS_QUANTITY);
    }
}