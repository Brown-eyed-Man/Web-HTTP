package ru.netology;

import ru.netology.handlers.GetHandler;
import ru.netology.handlers.PostHandler;

public class Main {
    private static final int PORT = 9999;
    private static final int THREADS_QUANTITY = 64;

    public static void main(String[] args) {
        Server server = new Server(THREADS_QUANTITY);

        server.addHandler(HttpMethod.GET, "/forms.html", new GetHandler());
        server.addHandler(HttpMethod.POST, "/forms.html", new PostHandler());

        server.start(PORT);
    }
}