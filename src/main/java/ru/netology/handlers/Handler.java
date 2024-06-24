package ru.netology.handlers;

import ru.netology.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

@FunctionalInterface
public interface Handler {
    void handle(Request request, BufferedOutputStream responseStream) throws IOException, URISyntaxException;
}
